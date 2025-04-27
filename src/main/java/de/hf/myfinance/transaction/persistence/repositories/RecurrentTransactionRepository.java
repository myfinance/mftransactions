package de.hf.myfinance.transaction.persistence.repositories;

import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface RecurrentTransactionRepository  extends ReactiveCrudRepository<RecurrentTransactionEntity, String> {
    Flux<RecurrentTransactionEntity> findByBudgetKeyOrAccKeyOrTrgBudgetKeyOrTrgAccKeyOrInsuranceKey
        (String budgetKey, String accKey, String trgBudgetKey, String trgAccKey, String insuranceKey);
}

