package de.hf.myfinance.transaction;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.DataReaderImpl;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.service.TransactionService;
import de.hf.testhelper.JsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
public class TransactionServiceTest extends EventProcessorTestBase{
    @Autowired
    TransactionService transactionService;

    @Test
    void createIncome() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);
        transactionService.addTransaction(transaction).block();

        final List<String> messages = getMessages(bindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(cashflows, data.get("cashflows"));
        assertEquals(TransactionType.INCOMEEXPENSES.toString(), data.get("transactionType"));
    }

    @Test
    void createIncomeFailsDueToNotExistingInstrument() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put("not existing budget", 100.0);
        cashflows.put("not existing Giro", 100.0);
        transaction.setCashflows(cashflows);

        assertThrows(MFException.class, () -> {
            transactionService.addTransaction(transaction).block();
        });
    }

    @Test
    void createIncomeFailsDueToDifferentTenant() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroOtherTenantKey, 100.0);
        transaction.setCashflows(cashflows);

        assertThrows(MFException.class, () -> {
            transactionService.addTransaction(transaction).block();
        });
    }

    @Test
    void updateIncome() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);
        transactionService.addTransaction(transaction).block();

        final List<String> messages = getMessages(bindingName);
        assertEquals(1, messages.size());

        var updatedTransaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var updatedCashflows = new HashMap<String, Double>();
        updatedCashflows.put(bgtKey, 200.0);
        updatedCashflows.put(giroKey, 200.0);
        updatedTransaction.setCashflows(updatedCashflows);
        updatedTransaction.setTransactionId("theId");
        transactionService.addTransaction(updatedTransaction).block();

        final List<String> messages2 = getMessages(bindingName);
        assertEquals(2, messages2.size());

        var eventTypes = new ArrayList<String>();
        eventTypes.add(validateUpdateEvents(updatedTransaction, messages2.get(0)));
        eventTypes.add(validateUpdateEvents(updatedTransaction, messages2.get(1)));
        assertTrue(eventTypes.contains("CREATE"));
        assertTrue(eventTypes.contains("DELETE"));
    }

    private String validateUpdateEvents(Transaction expectedTransaction, String msg) {
        JsonHelper jsonHelper = new JsonHelper();
        var eventType = (String)jsonHelper.convertJsonStringToMap(msg).get("eventType");
        assertTrue(eventType.equals("CREATE")||eventType.equals("DELETE"));
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap(msg).get("data");

        assertEquals(expectedTransaction.getTransactiondate().toString(), data.get("transactiondate"));
        assertEquals(expectedTransaction.getDescription(), data.get("description"));
        assertEquals(expectedTransaction.getCashflows(), data.get("cashflows"));
        assertEquals(TransactionType.INCOMEEXPENSES.toString(), data.get("transactionType"));

        if(eventType.equals("CREATE")){
            assertNull(data.get("transactionId"));
            return "CREATE";
        }
        if(eventType.equals("DELETE")){
            assertEquals("theId", data.get("transactionId"));
            return "DELETE";
        }
        return "wrong EventType";
    }

    @Test
    void createBudgetTransaction() {
        initDb();

        var desc = "testbudgettransfer";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.BUDGETTRANSFER);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, -100.0);
        cashflows.put(bgt2Key, 100.0);
        transaction.setCashflows(cashflows);
        transactionService.addTransaction(transaction).block();

        final List<String> messages = getMessages(bindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(cashflows, data.get("cashflows"));
        assertEquals(TransactionType.BUDGETTRANSFER.toString(), data.get("transactionType"));
    }

    @Test
    void createTransaction() {
        initDb();

        var desc = "testtransfer";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.TRANSFER);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(giroKey, -100.0);
        cashflows.put(giro2Key, 100.0);
        transaction.setCashflows(cashflows);
        transactionService.addTransaction(transaction).block();

        final List<String> messages = getMessages(bindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(cashflows, data.get("cashflows"));
        assertEquals(TransactionType.TRANSFER.toString(), data.get("transactionType"));
    }

}
