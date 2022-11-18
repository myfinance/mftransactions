package de.hf.myfinance.transaction;


import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.entities.TransactionEntity;
import de.hf.myfinance.transaction.persistence.repositories.TransactionRepository;
import de.hf.testhelper.MongoDbTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class PersistenceTests extends MongoDbTestBase {
    @Autowired
    TransactionRepository repository;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();
    }

    @Test
    void create() {
        var transaction = new TransactionEntity();
        transaction.setTransactiondate(LocalDate.of(2022, 1, 1));
        transaction.setTransactionType(TransactionType.INCOMEEXPENSES);
        transaction.setDescription("testeinkommen");
        var cashflows = new HashMap<String, Double>();
        cashflows.put("budget", 100.0);
        cashflows.put("giro", 100.0);
        transaction.setCashflows(cashflows);
        repository.save(transaction).block();
        assertEquals(1, repository.count().block());

        var transaction2 = new TransactionEntity();
        transaction2.setTransactiondate(LocalDate.of(2022, 2, 1));
        transaction2.setTransactionType(TransactionType.INCOMEEXPENSES);
        transaction2.setDescription("testeinkommen2");
        var cashflows2 = new HashMap<String, Double>();
        cashflows.put("budget", 100.0);
        cashflows.put("giro", 100.0);
        transaction.setCashflows(cashflows2);
        repository.save(transaction2).block();
        assertEquals(2, repository.count().block());

        assertEquals(1, repository.findByTransactiondateBetween(LocalDate.of(2022, 1, 2), LocalDate.of(2022, 2, 2)).collectList().block().size());

    }

}
