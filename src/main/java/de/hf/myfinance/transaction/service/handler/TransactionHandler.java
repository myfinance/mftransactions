package de.hf.myfinance.transaction.service.handler;

import reactor.core.publisher.Mono;

public interface TransactionHandler {
    Mono<String> validate();
}
