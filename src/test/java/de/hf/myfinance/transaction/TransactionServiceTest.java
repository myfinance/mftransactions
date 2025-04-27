package de.hf.myfinance.transaction;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.restmodel.*;
import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import de.hf.myfinance.transaction.persistence.entities.TransactionEntity;
import de.hf.myfinance.transaction.service.TransactionService;
import de.hf.testhelper.JsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
class TransactionServiceTest extends EventProcessorTestBase{
    @Autowired
    TransactionService transactionService;

    @Test
    void createIncome() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
    }

    @Test
    void createExpense() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.EXPENSE);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(-100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.EXPENSE.toString(), data.get("transactionType"));
    }

    @Test
    void createBuy() {
        initDb();

        var desc = "testbuy";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.BUY);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(-100.0);
        transaction.setAmount(10.0);
        transaction.setDepotBusinessKey(depotKey);
        transaction.setSecurityBusinessKey(equityKey);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(10.0, data.get("amount"));
        assertEquals(depotKey, data.get("depotBusinessKey"));
        assertEquals(equityKey, data.get("securityBusinessKey"));
        assertEquals(TransactionType.BUY.toString(), data.get("transactionType"));
    }

    @Test
    void createSell() {
        initDb();

        var desc = "testsell";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.SELL);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transaction.setAmount(10.0);
        transaction.setDepotBusinessKey(depotKey);
        transaction.setSecurityBusinessKey(equityKey);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(10.0, data.get("amount"));
        assertEquals(depotKey, data.get("depotBusinessKey"));
        assertEquals(equityKey, data.get("securityBusinessKey"));
        assertEquals(TransactionType.SELL.toString(), data.get("transactionType"));
    }

    @Test
    void createDividend() {
        initDb();

        var desc = "testdividend";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.DEPOTCASHFLOW);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transaction.setSecurityBusinessKey(equityKey);

        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(equityKey, data.get("securityBusinessKey"));
        assertEquals(TransactionType.DEPOTCASHFLOW.toString(), data.get("transactionType"));
    }

    @Test
    void createInterests() {
        initDb();

        var desc = "testinterest";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INTERESTS);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.INTERESTS.toString(), data.get("transactionType"));
    }

    @Test
    void createLifeinsuranceExpenses() {
        initDb();

        var desc = "test lifeinsurance expense";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.LIFEINSURANCEEXPENSE);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(-100.0);
        transaction.setInsuranceKey(lifeInsuranceKey);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(lifeInsuranceKey, data.get("insuranceKey"));
        assertEquals(TransactionType.LIFEINSURANCEEXPENSE.toString(), data.get("transactionType"));
    }

    @Test
    void createIncomeFailsDueToNotExistingInstrument() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey("not existing Giro");
        transaction.setBudgetKey("not existing budget");
        transaction.setValue(100.0);

        var transactionmono = transactionService.validateTransaction(transaction);
        assertThrows(MFException.class, () -> {
            transactionmono.block();
        });
    }

    @Test
    void createIncomeWithNegativeValue() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(-100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
    }
    @Test
    void createExpenseWithNegativeValue() {
        initDb();

        var desc = "testausgabe";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.EXPENSE);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(-100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.EXPENSE.toString(), data.get("transactionType"));
    }

    @Test
    void createIncomeFailsDueToDifferentTenant() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey(giroOtherTenantKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);

        var transactionmono = transactionService.validateTransaction(transaction);
        assertThrows(MFException.class, () -> {
            transactionmono.block();
        });
    }

    @Test
    void updateIncome() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new TransactionEntity(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transactionRepository.save(transaction).block();

        var id = transactionRepository.findAll().collectList().block().get(0).getTransactionId();

        var updatedTransaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        updatedTransaction.setAccKey(giroKey);
        updatedTransaction.setBudgetKey(bgtKey);
        updatedTransaction.setValue(200.0);
        updatedTransaction.setTransactionId(id);
        transactionService.validateTransaction(updatedTransaction).block();

        final List<String> messages2 = getMessages(transactionApprovedBindingName);
        assertEquals(2, messages2.size());

        var eventTypes = new ArrayList<String>();
        // i have to set the id again because it was set to null to create a new id for the insert but I want to compare it with the expected
        updatedTransaction.setTransactionId(id);
        eventTypes.add(validateUpdateEvents(updatedTransaction, messages2.get(0)));
        eventTypes.add(validateUpdateEvents(updatedTransaction, messages2.get(1)));
        assertTrue(eventTypes.contains("CREATE"));
        assertTrue(eventTypes.contains("DELETE"));
    }

    private String validateUpdateEvents(Transaction expectedTransaction, String msg) {
        JsonHelper jsonHelper = new JsonHelper();
        var eventType = (String)jsonHelper.convertJsonStringToMap(msg).get("eventType");
        assertTrue(eventType.equals("CREATE")||eventType.equals("DELETE"));
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap(msg).get("data");

        assertEquals(expectedTransaction.getTransactiondate().toString(), data.get("transactiondate"));
        assertEquals(expectedTransaction.getDescription(), data.get("description"));
        assertEquals(expectedTransaction.getAccKey(), data.get("accKey"));
        assertEquals(expectedTransaction.getBudgetKey(), data.get("budgetKey"));
        assertEquals(expectedTransaction.getValue(), data.get("value"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));

        if(eventType.equals("CREATE")){
            assertNull(data.get("transactionId"));
            return "CREATE";
        }
        if(eventType.equals("DELETE")){
            assertEquals(expectedTransaction.getTransactionId(), data.get("transactionId"));
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
        transaction.setTrgBudgetKey(bgt2Key);
        transaction.setBudgetKey(bgtKey);
        transaction.setValue(100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(bgt2Key, data.get("trgBudgetKey"));
        assertEquals(bgtKey, data.get("budgetKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.BUDGETTRANSFER.toString(), data.get("transactionType"));
    }

    @Test
    void createTransaction() {
        initDb();

        var desc = "testtransfer";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.TRANSFER);
        transaction.setAccKey(giroKey);
        transaction.setTrgAccKey(giro2Key);
        transaction.setValue(100.0);
        transactionService.validateTransaction(transaction).block();

        final List<String> messages = getMessages(transactionApprovedBindingName);
        assertEquals(1, messages.size());

        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap<String, Object>)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(transactionDate.toString(), data.get("transactiondate"));
        assertEquals(desc, data.get("description"));
        assertEquals(giroKey, data.get("accKey"));
        assertEquals(giro2Key, data.get("trgAccKey"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.TRANSFER.toString(), data.get("transactionType"));
    }


    @Test
    void listRecurrentTransactions() {
        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransactionEntity();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setBudgetKey(bgtKey);
        recurrentTransaction.setTrgBudgetKey(bgt2Key);
        recurrentTransaction.setValue(100);

        recurrentTransactionRepository.save(recurrentTransaction).block();
        var transactions = transactionService.listRecurrentTransactions().collectList().block();

        assertEquals(1, transactions.size());
    }

    @Test
    void createIncomeOnInactiveBudget() {
        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOME);
        transaction.setAccKey(giroKey);
        transaction.setBudgetKey(inactivebgtKey);
        transaction.setValue(100.0);
        var transactionmono = transactionService.validateTransaction(transaction);
        assertThrows(MFException.class, () -> {
            transactionmono.block();
        });
    }




}
