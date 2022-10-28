package de.hf.myfinance.transaction.persistence.repositories;

import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.entities.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TransactionRepository  extends ReactiveCrudRepository<TransactionEntity, String> {
}
