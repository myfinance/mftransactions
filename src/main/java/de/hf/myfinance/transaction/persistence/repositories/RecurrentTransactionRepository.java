package de.hf.myfinance.transaction.persistence.repositories;

import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RecurrentTransactionRepository  extends ReactiveCrudRepository<RecurrentTransactionEntity, String> {
}
