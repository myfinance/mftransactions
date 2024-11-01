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

    public Mono<Transaction> validate() {
        validateTransactionDate(transaction.getTransactiondate());
        validateTransactionDesc(transaction.getDescription());
        return validateTransactionId(transaction)
            .flatMap(this::validateCashflows)
            .flatMap(this::additionalValidation)
            .flatMap(this::saveTransaction);
    }

    protected Mono<Transaction> additionalValidation(Transaction transaction){
        return Mono.just(transaction);
    }

    protected Mono<Transaction> validateCashflows(Transaction transaction){
        var cashflows = transaction.getCashflows();
        validateCashflowNumber(cashflows);
        validateCashflowValue(cashflows);
        return this.transactionEnvironment.getDataReader().findInstrumentByBusinesskeyIn(cashflows.keySet())
                .collectList().flatMap(this::validateInstruments)
                .flatMap(i->Mono.just(transaction));
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
        validateInstrumentStatus(instruments);
        return Mono.just("valid Transaction");
    }

    protected abstract void validateInstrumentTypes(List<Instrument> instruments);

    protected void validateInstrumentStatus(List<Instrument> instruments){
        instruments.forEach(i->{
            if(!i.isActive()){
                throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, "No new Transactions allowd for inactive instruments:"+ i);
            }
        });
    }
    
    protected Mono<Transaction> saveTransaction(Transaction transaction) {
        if(transaction.getTransactionId()!=null && !transaction.getTransactionId().isEmpty()){
            transactionEnvironment.getAuditService().saveMessage(transaction+" deleted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
            transactionEnvironment.getEventHandler().sendDeleteTransactionEvent(transaction);
            transaction.setTransactionId(null);
        }
        transactionEnvironment.getAuditService().saveMessage(transaction+" inserted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
        transactionEnvironment.getEventHandler().sendTransactionApprovedEvent(transaction);
        return Mono.just(transaction);
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

    protected Mono<Transaction> validateTransactionId(Transaction transaction){
        if(transaction.getTransactionId() != null && transaction.getTransactionId().trim().isEmpty()) {
            transaction.setTransactionId(null);
        }
        if(transaction.getTransactionId()!=null && !transaction.getTransactionId().isEmpty()) {
            return this.transactionEnvironment.getDataReader().findTransactionById(transaction.getTransactionId())
                    .switchIfEmpty(handleNotExistingTransaction()).flatMap(i-> Mono.just(transaction));
        }
        return Mono.just(transaction);
    }

    private Mono<Transaction> handleNotExistingTransaction(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_TRANSACTION_EXCEPTION, "No transaction for this transactionId available:"+transaction.getTransactionId()));
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
