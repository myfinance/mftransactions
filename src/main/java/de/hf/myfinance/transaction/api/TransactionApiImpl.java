package de.hf.myfinance.transaction.api;

import de.hf.myfinance.restapi.TransactionApi;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Trade;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import de.hf.framework.utils.ServiceUtil;
import reactor.core.publisher.Mono;

@RestController
public class TransactionApiImpl implements TransactionApi {
    ServiceUtil serviceUtil;
    TransactionService transactionService;

    @Value("${api.common.version}")
    String apiVersion;

    @Autowired
    public TransactionApiImpl(ServiceUtil serviceUtil, TransactionService transactionService) {
        this.serviceUtil = serviceUtil;
        this.transactionService = transactionService;
    }

    @Override
    public String index() {
        return "Hello my TransactionService version:"+apiVersion;
    }

    @Override
    public Mono<String> addTrade(Trade trade) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> updateTrade(Trade trade) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> delRecurrentTransfer(String recurrentTransactionId) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> delTransfer(String transactionId) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> updateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> addRecurrentTransaction(RecurrentTransaction recurrentTransaction) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Mono<String> addTransaction(Transaction transaction) {
        return transactionService.addTransaction(transaction);
    }

    @Override
    public Mono<String> updateTransaction(Transaction transaction) {
        return Mono.just("not implemented yet");
    }

}