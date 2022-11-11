package de.hf.myfinance.transaction.persistence.repositories;

import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.entities.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface TransactionRepository  extends ReactiveCrudRepository<TransactionEntity, String> {

    Flux<Transaction> findByTransactiondateBetween(LocalDate startDate, LocalDate endDate);
}
