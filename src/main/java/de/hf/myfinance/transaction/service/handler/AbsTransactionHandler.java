package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.Severity;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Flux;
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
        return validateInstruments(transaction)
            .flatMap(this::validateValue)
            .flatMap(this::additionalValidation)
            .flatMap(this::handleOldTransaction)
            .flatMap(this::saveTransaction);
    }

    protected Mono<Transaction> additionalValidation(Transaction transaction){
        return Mono.just(transaction);
    }

    protected Mono<Transaction> validateValue(Transaction transaction){
        if(transaction.getValue()==0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, "value = 0 not allowed");
        }
        if(transaction.getValue()<0) {
            transaction.setValue(transaction.getValue()*(-1));
        }
        return Mono.just(transaction);
    }
    protected abstract Mono<Transaction> validateInstruments(Transaction transaction);

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

    protected Mono<String> validateInstrumentTypes(Map<String, List<InstrumentType>> instrumentKeyTypeMap){
        return Flux.fromIterable(instrumentKeyTypeMap.entrySet())
            .flatMap(entry -> validateInstrument(entry.getKey(), entry.getValue()))
            .collectList()
            .flatMap(this::validateTenant);
    }

    protected Mono<Instrument> validateInstrument(String instrumentKey, List<InstrumentType> instrumentTypes){

       return this.transactionEnvironment.getDataReader().findByBusinesskey(instrumentKey)
            .switchIfEmpty(handleNotExistingInstrument())
            .flatMap(i->validateInstrumentType(i, instrumentTypes))
            .flatMap(this::validateIsActive);
    }

    private Mono<Instrument> validateInstrumentType(Instrument instrument, List<InstrumentType> instrumentTypes){
        if(!instrumentTypes.contains(instrument.getInstrumentType())){
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttype for "+this.getClass().getName());
        }
        return Mono.just(instrument);
    }
    private Mono<Instrument> validateIsActive(Instrument instrument){
        if(!instrument.isActive()){
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, "No new Transactions allowd for inactive instruments:"+ instrument.getDescription());
        }
        return Mono.just(instrument);
    }
    
    protected Mono<Transaction> saveTransaction(Transaction transaction) {
        transactionEnvironment.getAuditService().saveMessage(transaction+" inserted: " + transaction, Severity.INFO, AUDIT_MSG_TYPE);
        transactionEnvironment.getEventHandler().sendTransactionApprovedEvent(transaction);
        return Mono.just(transaction);
    }

    protected Mono<Transaction> handleOldTransaction(Transaction transaction){
        if(transaction.getTransactionId() != null && transaction.getTransactionId().trim().isEmpty()) {
            transaction.setTransactionId(null);
        }
        if(transaction.getTransactionId()!=null && !transaction.getTransactionId().isEmpty()) {
            return this.transactionEnvironment.getDataReader().findTransactionById(transaction.getTransactionId())
                    .switchIfEmpty(handleNotExistingTransaction())
                    .flatMap(oldTransaction-> {
                        transactionEnvironment.getAuditService().saveMessage(transaction+" deleted: " + oldTransaction, Severity.INFO, AUDIT_MSG_TYPE);
                        transactionEnvironment.getEventHandler().sendDeleteTransactionEvent(oldTransaction);
                        transaction.setTransactionId(null);
                        return Mono.just(transaction);
                    });
        }
        return Mono.just(transaction);
    }

    private Mono<Transaction> handleNotExistingTransaction(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_TRANSACTION_EXCEPTION, "No transaction for this transactionId available:"+transaction.getTransactionId()));
    }

    private Mono<Instrument> handleNotExistingInstrument(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "Not all necessary instruments available for this transaction:"+transaction.getTransactionId()));
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
