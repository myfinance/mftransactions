package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.Severity;
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
    protected static final String AUDIT_MSG_TYPE="TransactionHandler_User_Event";

    public AbsTransactionHandler(TransactionEnvironment transactionEnvironment, Transaction transaction){
        this.transactionEnvironment = transactionEnvironment;
        this.transaction = transaction;
    }

    public Mono<String> validate() {
        validateTransactionDate(transaction.getTransactiondate());
        validateTransactionDesc(transaction.getDescription());
        return validateCashflows(transaction.getCashflows());
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

    protected abstract Mono<String> validateCashflows(Map<String, Double> cashflows);

    protected Mono<String> saveTransaction(String msg) {
        transactionEnvironment.getAuditService().saveMessage(transaction+" inserted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
        transactionEnvironment.getEventHandler().sendTransactionApprovedEvent(transaction);
        return Mono.just("new transaction approved:" + transaction);
    }
}
