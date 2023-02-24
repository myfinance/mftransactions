package de.hf.myfinance.transaction;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class SaveRecurrentTransactionProcessorTest extends EventProcessorTestBase {
    @Autowired
    @Qualifier("saveRecurrentTransactionProcessor")
    protected Consumer<Event<String, RecurrentTransaction>> saveRecurrentTransactionProcessor;

    @Test
    void createRecurrentTransaction() {

        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);
        recurrentTransaction.setValue(100);

        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.hashCode(), recurrentTransaction);
        saveRecurrentTransactionProcessor.accept(creatEvent);

        var recurrentTransactions = recurrentTransactionRepository.findAll().collectList().block();
        assertEquals(1, recurrentTransactions.size());

        var savedRecurrentTransactions = recurrentTransactions.get(0);
        assertNotNull(savedRecurrentTransactions.getRecurrentTransactionId());
        assertEquals(TransactionType.INCOME, savedRecurrentTransactions.getTransactionType());
        assertEquals(nextTransactiondate, savedRecurrentTransactions.getNextTransactionDate());
        assertEquals(RecurrentFrequency.MONTHLY, savedRecurrentTransactions.getRecurrentFrequency());
        assertEquals(giroKey, savedRecurrentTransactions.getFirstInstrumentBusinessKey());
        assertEquals(bgtKey, savedRecurrentTransactions.getSecondInstrumentBusinessKey());
        assertEquals(100, savedRecurrentTransactions.getValue());
    }

    @Test
    void updateRecurrentTransaction() {

        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);
        recurrentTransaction.setValue(100);

        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.hashCode(), recurrentTransaction);
        saveRecurrentTransactionProcessor.accept(creatEvent);

        var recurrentTransactions = recurrentTransactionRepository.findAll().collectList().block();
        var savedRecurrentTransactions = recurrentTransactions.get(0);
        recurrentTransaction.setRecurrentTransactionId(savedRecurrentTransactions.getRecurrentTransactionId());
        recurrentTransaction.setValue(200);

        creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.hashCode(), recurrentTransaction);
        saveRecurrentTransactionProcessor.accept(creatEvent);

        recurrentTransactions = recurrentTransactionRepository.findAll().collectList().block();
        assertEquals(1, recurrentTransactions.size());

        savedRecurrentTransactions = recurrentTransactions.get(0);
        assertNotNull(savedRecurrentTransactions.getRecurrentTransactionId());
        assertEquals(TransactionType.INCOME, savedRecurrentTransactions.getTransactionType());
        assertEquals(nextTransactiondate, savedRecurrentTransactions.getNextTransactionDate());
        assertEquals(RecurrentFrequency.MONTHLY, savedRecurrentTransactions.getRecurrentFrequency());
        assertEquals(giroKey, savedRecurrentTransactions.getFirstInstrumentBusinessKey());
        assertEquals(bgtKey, savedRecurrentTransactions.getSecondInstrumentBusinessKey());
        assertEquals(200, savedRecurrentTransactions.getValue());
    }

    @Test
    void deleteRecurrentTransaction() {

        initDb();

        var nextTransactiondate = LocalDate.now().plusMonths(1);
        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);
        recurrentTransaction.setValue(100);

        Event creatEvent = new Event(Event.Type.CREATE, recurrentTransaction.hashCode(), recurrentTransaction);
        saveRecurrentTransactionProcessor.accept(creatEvent);

        var recurrentTransactions = recurrentTransactionRepository.findAll().collectList().block();
        var savedRecurrentTransactions = recurrentTransactions.get(0);
        recurrentTransaction.setRecurrentTransactionId(savedRecurrentTransactions.getRecurrentTransactionId());

        creatEvent = new Event(Event.Type.DELETE, recurrentTransaction.hashCode(), recurrentTransaction);
        saveRecurrentTransactionProcessor.accept(creatEvent);

        recurrentTransactions = recurrentTransactionRepository.findAll().collectList().block();
        assertEquals(0, recurrentTransactions.size());

    }

    @Test
    void deleteNoExistingRecurrentTransaction() {

        initDb();

        var recurrentTransaction = new RecurrentTransaction();
        recurrentTransaction.setRecurrentTransactionId("bla");


        Event creatEvent = new Event(Event.Type.DELETE, recurrentTransaction.hashCode(), recurrentTransaction);

        assertThrows(MFException.class, () -> {
            saveRecurrentTransactionProcessor.accept(creatEvent);
        });
    }
}
