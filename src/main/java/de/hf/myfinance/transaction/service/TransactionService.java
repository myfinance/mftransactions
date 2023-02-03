package de.hf.myfinance.transaction.service;

import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.handler.RecurrentTransactionHandler;
import de.hf.myfinance.transaction.service.handler.TransactionHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
public class TransactionService {

    private final TransactionHandlerFactory transactionHandlerFactory;
    private final RecurrentTransactionHandler recurrentTransactionHandler;

    @Autowired
    public TransactionService(TransactionHandlerFactory transactionHandlerFactory, RecurrentTransactionHandler recurrentTransactionHandler){
        this.transactionHandlerFactory = transactionHandlerFactory;
        this.recurrentTransactionHandler = recurrentTransactionHandler;
    }

    public Mono<String> validateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {
        return recurrentTransactionHandler.validateRecurrentTransaction(recurrentTransaction);
    }

    public Mono<String> addTransaction(Transaction transaction) {
        return transactionHandlerFactory.createTransactionHandler(transaction).validate();
    }

    public Flux<Transaction> listTransactions(LocalDate startDate, LocalDate endDate) {
        return transactionHandlerFactory.listTransactions(startDate, endDate);
    }

    public Flux<RecurrentTransaction> listRecurrentTransactions() {
        return null;
    }
}
