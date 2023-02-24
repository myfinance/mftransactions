package de.hf.myfinance.transaction;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.RecurrentFrequency;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import de.hf.testhelper.JsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
public class ProcessRecurrentTransactionsProcessorTest extends EventProcessorTestBase {
    @Autowired
    @Qualifier("processRecurrentTransactionsProcessor")
    protected Consumer<Event<String, RecurrentTransaction>> processRecurrentTransactionsProcessor;

    @Test
    void processRecurrentTransactions() {
        initDb();

        var nextTransactiondate = LocalDate.now().minusDays(1);
        var recurrentTransaction = new RecurrentTransactionEntity();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setDescription("test");
        recurrentTransaction.setValue(100);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);

        recurrentTransactionRepository.save(recurrentTransaction).block();

        Event creatEvent = new Event(Event.Type.START, recurrentTransaction.toString(), recurrentTransaction);
        processRecurrentTransactionsProcessor.accept(creatEvent);


        final List<String> messages = getMessages(validateTransactionBindingName);
        assertEquals(1, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals("test", data.get("description"));
        assertEquals(nextTransactiondate.toString(), data.get("transactiondate"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
        var savedCashflows = (Map<String, Double>)data.get("cashflows");
        assertEquals(2, savedCashflows.size());
        assertEquals(100, savedCashflows.get(giroKey));
        assertEquals(100, savedCashflows.get(bgtKey));


        final List<String> recurrenttransactionUpdateMessages = getMessages(recurrentTransactionApprovedBindingName);
        assertEquals(1, recurrenttransactionUpdateMessages.size());
        data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((recurrenttransactionUpdateMessages.get(0))).get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgtKey, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.plusMonths(1).toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals("test", data.get("description"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
    }

    @Test
    void processRecurrentTransactionsMultipleTrasnactions() {
        initDb();

        var nextTransactiondate = LocalDate.now().minusDays(1).minusMonths(1);
        var recurrentTransaction = new RecurrentTransactionEntity();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setDescription("test");
        recurrentTransaction.setValue(100);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);

        recurrentTransactionRepository.save(recurrentTransaction).block();

        final List<String> bla = getMessages(recurrentTransactionApprovedBindingName);

        Event creatEvent = new Event(Event.Type.START, recurrentTransaction.toString(), recurrentTransaction);
        processRecurrentTransactionsProcessor.accept(creatEvent);

        final List<String> messages = getMessages(validateTransactionBindingName);
        assertEquals(2, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals("test", data.get("description"));
        assertEquals(nextTransactiondate.toString(), data.get("transactiondate"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
        var savedCashflows = (Map<String, Double>)data.get("cashflows");
        assertEquals(2, savedCashflows.size());
        assertEquals(100, savedCashflows.get(giroKey));
        assertEquals(100, savedCashflows.get(bgtKey));

        data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(1))).get("data");
        assertEquals("test", data.get("description"));
        assertEquals(nextTransactiondate.plusMonths(1).toString(), data.get("transactiondate"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
        savedCashflows = (Map<String, Double>)data.get("cashflows");
        assertEquals(2, savedCashflows.size());
        assertEquals(100, savedCashflows.get(giroKey));
        assertEquals(100, savedCashflows.get(bgtKey));


        final List<String> recurrenttransactionUpdateMessages = getMessages(recurrentTransactionApprovedBindingName);
        assertEquals(1, recurrenttransactionUpdateMessages.size());
        data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((recurrenttransactionUpdateMessages.get(0))).get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgtKey, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.plusMonths(2).toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals("test", data.get("description"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
    }
}
