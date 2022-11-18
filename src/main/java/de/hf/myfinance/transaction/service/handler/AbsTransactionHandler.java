package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.Severity;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public abstract class AbsTransactionHandler implements TransactionHandler {

    protected final TransactionEnvironment transactionEnvironment;
    protected final Transaction transaction;
    protected static final String AUDIT_MSG_TYPE="TransactionHandler_User_Event";

    protected AbsTransactionHandler(TransactionEnvironment transactionEnvironment, Transaction transaction){
        this.transactionEnvironment = transactionEnvironment;
        this.transaction = transaction;
    }

    public Mono<String> validate() {
        validateTransactionDate(transaction.getTransactiondate());
        validateTransactionDesc(transaction.getDescription());
        return validateCashflows(transaction.getCashflows())
                .flatMap(this::validateExistingTransaction);
    }

    protected Mono<String> validateCashflows(Map<String, Double> cashflows){
        validateCashflowNumber(cashflows);
        validateCashflowValue(cashflows);
        return this.transactionEnvironment.getDataReader().findInstrumentByBusinesskeyIn(cashflows.keySet())
                .collectList().flatMap(i->validateInstruments(i))
                .flatMap(this::saveTransaction);
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

    protected Mono<String> validateInstruments(List<Instrument> instruments){
        validateInstrumentNumber(instruments);
        validateTenant(instruments);
        validateInstrumentTypes(instruments);
        return Mono.just("valid Transaction");
    }

    protected abstract void validateInstrumentTypes(List<Instrument> instruments);

    protected Mono<String> validateExistingTransaction(String msg){
        if(transaction.getTransactionId()!=null && !transaction.getTransactionId().isEmpty()) {
            return this.transactionEnvironment.getDataReader().findTransactiondateById(transaction.getTransactionId())
                    .switchIfEmpty(handleNotExistingTransaction()).flatMap(i-> Mono.just("Update approved"));
        }
        return Mono.just(msg);

    }

    private Mono<Transaction> handleNotExistingTransaction(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_TRANSACTION_EXCEPTION, "No transaction for this transactionId available:"+transaction.getTransactionId()));
    }

    protected Mono<String> saveTransaction(String msg) {
        msg = "new transaction approved:" + transaction;
        if(transaction.getTransactionId()!=null && !transaction.getTransactionId().isEmpty()){
            msg = "transaction update approved:" + transaction;
            transactionEnvironment.getAuditService().saveMessage(transaction+" deleted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
            transactionEnvironment.getEventHandler().sendDeleteTransactionEvent(transaction);
            transaction.setTransactionId(null);
        }
        transactionEnvironment.getAuditService().saveMessage(transaction+" inserted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
        transactionEnvironment.getEventHandler().sendTransactionApprovedEvent(transaction);
        return Mono.just(msg);
    }

    protected void validateCashflowNumber(Map<String, Double> cashflows) {
        if(cashflows ==null || cashflows.isEmpty() || cashflows.size()!=2) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid cashflows:"+ cashflows);
        }
    }

    protected void validateCashflowValue(Map<String, Double> cashflows) {
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1)*(-1))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+ cashflows);
        }
    }

    protected void validateInstrumentNumber(List<Instrument> instruments) {
        if(instruments.size()!=2){
            throw new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "Not all Instruments for this transaction available.");
        }
    }

    protected Mono<String> validateTenant(List<Instrument> instruments) {
        var tenant = instruments.get(0).getTenantBusinesskey();
        instruments.forEach(i->{
            if(!i.getTenantBusinesskey().equals(tenant)){
                throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Instruments have not the same tenant for transaction");
            }
        });
        return Mono.just("valid");
    }
}
