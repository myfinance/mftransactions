package de.hf.myfinance.transaction;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class SaveTransactionProcessorTest extends EventProcessorTestBase {

    @Autowired
    @Qualifier("saveTransactionProcessor")
    protected Consumer<Event<String, Transaction>> saveTransactionProcessor;




    @Test
    void createTransaction() {

        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);

        Event creatEvent = new Event(Event.Type.CREATE, transaction.hashCode(), transaction);
        saveTransactionProcessor.accept(creatEvent);

        var transactions = transactionRepository.findAll().collectList().block();
        assertEquals(1, transactions.size());

        var savedtransaction = transactions.get(0);
        assertNotNull(savedtransaction.getTransactionId());
        assertEquals(desc, savedtransaction.getDescription());
        assertEquals(TransactionType.INCOMEEXPENSES, savedtransaction.getTransactionType());
        assertEquals(transactionDate, savedtransaction.getTransactiondate());
        assertEquals(2, savedtransaction.getCashflows().size());
    }

    @Test
    void deleteTransaction() {

        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);

        Event creatEvent = new Event(Event.Type.CREATE, transaction.hashCode(), transaction);
        saveTransactionProcessor.accept(creatEvent);

        var transactions = transactionRepository.findAll().collectList().block();
        assertEquals(1, transactions.size());

        var savedtransaction = transactions.get(0);
        assertNotNull(savedtransaction.getTransactionId());
        assertEquals(desc, savedtransaction.getDescription());
        assertEquals(TransactionType.INCOMEEXPENSES, savedtransaction.getTransactionType());
        assertEquals(transactionDate, savedtransaction.getTransactiondate());
        assertEquals(2, savedtransaction.getCashflows().size());

        transaction.setTransactionId(savedtransaction.getTransactionId());

        Event deleteEvent = new Event(Event.Type.DELETE, transaction.hashCode(), transaction);
        saveTransactionProcessor.accept(deleteEvent);

        transactions = transactionRepository.findAll().collectList().block();
        assertEquals(0, transactions.size());
    }

    @Test
    void duplicateTransaction() {

        initDb();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);

        Event creatEvent = new Event(Event.Type.CREATE, transaction.hashCode(), transaction);
        saveTransactionProcessor.accept(creatEvent);

        saveTransactionProcessor.accept(creatEvent);

        var transactions = transactionRepository.findAll().collectList().block();
        assertEquals(2, transactions.size());
    }
}
