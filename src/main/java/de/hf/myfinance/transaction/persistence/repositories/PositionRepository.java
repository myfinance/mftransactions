package de.hf.myfinance.transaction.persistence.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;
import de.hf.myfinance.transaction.persistence.entities.PositionEntity;
import de.hf.myfinance.transaction.persistence.entities.PositionKey;

public interface PositionRepository  extends ReactiveCrudRepository<PositionEntity, String> {
    Mono<PositionEntity> findByPositionKey(PositionKey positionKey);
    Mono<Long> deleteByPositionKey(PositionKey positionKey);
}