package de.hf.myfinance.transaction;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.RecurrentFrequency;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.TransactionType;
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
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
class ValidateRecurrentTransactionProcessorTest extends EventProcessorTestBase{

    @Autowired
    @Qualifier("validateRecurrentTransactionProcessor")
    protected Consumer<Event<String, RecurrentTransaction>> validateRecurrentTransactionProcessor;

    @Test
    void validateRecurrentIncome() {
        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setValue(100);


        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.toString(), recurrentTransaction);
        validateRecurrentTransactionProcessor.accept(creatEvent);

        final List<String> messages = getMessages("recurrentTransactionApproved-out-0");
        assertEquals(1, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgtKey, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));
    }

    @Test
    void validateRecurrentExpense() {
        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setValue(-100);


        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.toString(), recurrentTransaction);
        validateRecurrentTransactionProcessor.accept(creatEvent);

        final List<String> messages = getMessages("recurrentTransactionApproved-out-0");
        assertEquals(1, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgtKey, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.toString(), data.get("nextTransactionDate"));
        assertEquals(-100.0, data.get("value"));
        assertEquals(TransactionType.EXPENSE.toString(), data.get("transactionType"));
    }

    @Test
    void validateRecurrentTransfer() {
        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(giro2Key);
        recurrentTransaction.setValue(100);


        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.toString(), recurrentTransaction);
        validateRecurrentTransactionProcessor.accept(creatEvent);

        final List<String> messages = getMessages("recurrentTransactionApproved-out-0");
        assertEquals(1, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(giro2Key, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.TRANSFER.toString(), data.get("transactionType"));
    }

    @Test
    void validateRecurrentBudgetTransfer() {
        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgt2Key);
        recurrentTransaction.setValue(100);


        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.toString(), recurrentTransaction);
        validateRecurrentTransactionProcessor.accept(creatEvent);

        final List<String> messages = getMessages("recurrentTransactionApproved-out-0");
        assertEquals(1, messages.size());
        LOG.info(messages.get(0));
        JsonHelper jsonHelper = new JsonHelper();
        var data = (LinkedHashMap)jsonHelper.convertJsonStringToMap((messages.get(0))).get("data");
        assertEquals(bgtKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgt2Key, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals(TransactionType.BUDGETTRANSFER.toString(), data.get("transactionType"));
    }
}
