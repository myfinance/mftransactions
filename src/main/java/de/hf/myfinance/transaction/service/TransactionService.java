package de.hf.myfinance.transaction.service;

import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.handler.TransactionHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TransactionService {

    private final TransactionHandlerFactory transactionHandlerFactory;

    @Autowired
    public TransactionService(TransactionHandlerFactory transactionHandlerFactory){
        this.transactionHandlerFactory = transactionHandlerFactory;
    }



    public Mono<String> addTransaction(Transaction transaction) {
        return transactionHandlerFactory.createTransactionHandler(transaction).validate();
    }


}
