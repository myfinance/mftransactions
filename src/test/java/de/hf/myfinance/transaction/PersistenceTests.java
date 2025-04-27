package de.hf.myfinance.transaction;

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
        transaction.setTransactionType(TransactionType.INCOME);
        transaction.setDescription("testeinkommen");
        transaction.setAccKey("giro");
        transaction.setBudgetKey("budget");
        transaction.setValue(100.0);
        repository.save(transaction).block();
        assertEquals(1, repository.count().block());

        var transaction2 = new TransactionEntity();
        transaction2.setTransactiondate(LocalDate.of(2022, 2, 1));
        transaction2.setTransactionType(TransactionType.INCOME);
        transaction2.setDescription("testeinkommen2");
        transaction2.setAccKey("giro");
        transaction2.setBudgetKey("budget");
        transaction2.setValue(100.0);
        repository.save(transaction2).block();
        assertEquals(2, repository.count().block());

        assertEquals(1, repository.findByTransactiondateBetween(LocalDate.of(2022, 1, 2), LocalDate.of(2022, 2, 2)).collectList().block().size());

    }

}
