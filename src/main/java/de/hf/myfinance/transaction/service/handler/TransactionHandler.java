package de.hf.myfinance.transaction.service.handler;

import de.hf.myfinance.restmodel.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionHandler {
    Mono<Transaction> validate();
}
