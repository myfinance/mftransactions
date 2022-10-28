package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

public abstract class AbsTransactionHandler implements TransactionHandler {

    protected final TransactionEnvironment transactionEnvironment;
    protected final Transaction transaction;

    public AbsTransactionHandler(TransactionEnvironment transactionEnvironment, Transaction transaction){
        this.transactionEnvironment = transactionEnvironment;
        this.transaction = transaction;
    }

    public Mono<String> validate() {
        validateTransactionDate(transaction.getTransactiondate());
        validateTransactionDesc(transaction.getDescription());
        validateCashflows(transaction.getCashflows());
        return Mono.just("not done yet");
    }

    protected void validateTransactionDate(LocalDate transactiondate) {
        if(transactiondate.isAfter(LocalDate.now()) || transactiondate.isBefore(LocalDate.of(2000,1,1))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid transactiondate:"+transactiondate);
        }
    }

    protected void validateTransactionDesc(String desc) {
        if(desc==null || desc.isEmpty()) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid description:"+desc);
        }
    }

    protected abstract void validateCashflows(Map<String, Double> cashflows);
}
