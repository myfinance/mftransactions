package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.*;
import de.hf.myfinance.transaction.events.out.RecurrentTransactionApprovedEventHandler;
import de.hf.myfinance.transaction.events.out.ValidateTransactionEventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecurrentTransactionHandler {

    private AuditService auditService;
    private static final String AUDIT_MSG_TYPE="RecurrentTransaction_User_Event";
    private final RecurrentTransactionApprovedEventHandler recurrentTransactionApprovedEventHandler;

    private final ValidateTransactionEventHandler validateTransactionEventHandler;

    private DataReader dataReader;

    public RecurrentTransactionHandler(AuditService auditService, DataReader dataReader, RecurrentTransactionApprovedEventHandler recurrentTransactionApprovedEventHandler, ValidateTransactionEventHandler validateTransactionEventHandler) {
        this.auditService = auditService;
        this.dataReader = dataReader;
        this.recurrentTransactionApprovedEventHandler = recurrentTransactionApprovedEventHandler;
        this.validateTransactionEventHandler = validateTransactionEventHandler;
    }

    public Mono<String> process() {
        return dataReader.findRecurrentTransactions().collectList().flatMap(this::processRecurrentTransactions);
    }

    private Mono<String> processRecurrentTransactions(List<RecurrentTransaction> recurrentTransactions){
        LocalDateTime ts = LocalDateTime.now();
        recurrentTransactions.forEach(i-> {
            LocalDate nextTransaction = i.getNextTransactionDate();

            while(nextTransaction.isBefore(ts.toLocalDate())) {
                validateTransactionEventHandler.sendTransactionValidationRequestEvent(buildTransaction(ts, i, nextTransaction));


                nextTransaction = calcNextTransaction(nextTransaction, i.getRecurrentFrequency());
            }
            i.setNextTransactionDate(nextTransaction);
            recurrentTransactionApprovedEventHandler.sendRecurrentTransactionApprovedEvent(i);
        });
        auditService.saveMessage("recurrent transactions booked", Severity.INFO, AUDIT_MSG_TYPE);
        return Mono.just("recurrentTransactions processed");
    }

    private LocalDate calcNextTransaction(LocalDate lastTransaction, RecurrentFrequency frequency) {
        switch(frequency) {
            case MONTHLY:
                return lastTransaction.plusMonths(1);
            case QUARTERLY:
                return lastTransaction.plusMonths(3);
            case YEARLY:
                return lastTransaction.plusYears(1);
            default:
                return lastTransaction.plusMonths(1);
        }
    }

    private Transaction buildTransaction(LocalDateTime ts, RecurrentTransaction i, LocalDate nextTransaction) {
        var transaction = new Transaction();
        transaction.setDescription(i.getDescription());
        transaction.setTransactionType(i.getTransactionType());
        transaction.setLastchanged(ts);
        transaction.setTransactiondate(nextTransaction);
        transaction.setValue(i.getValue());
        transaction.setAccKey(i.getAccKey());
        transaction.setBudgetKey(i.getBudgetKey());
        transaction.setInsuranceKey(i.getInsuranceKey());
        transaction.setTrgAccKey(i.getTrgAccKey());
        transaction.setTrgBudgetKey(i.getTrgBudgetKey());

        return transaction;
    }

    public Mono<String> validateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {

        return validateInstruments(recurrentTransaction)
                .flatMap(this::validateFrequency)
                .flatMap(this::validateNextTransactionDate)
                .flatMap(this::validateRecurrentTransactionId)
                .flatMap(this::recurrentTransactionApproved);
    }

    private Mono<RecurrentTransaction> validateInstruments(RecurrentTransaction recurrentTransaction) {
        if(recurrentTransaction.getTransactionType()==null) {
            return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "no recurrenttransactiontype"));
        }
        if(recurrentTransaction.getTransactionType().equals(TransactionType.INCOME)
            || recurrentTransaction.getTransactionType().equals(TransactionType.EXPENSE)){
            return getInstrument(recurrentTransaction.getAccKey())
                .flatMap(accInstrument->validateCashInstrument(accInstrument))
                .flatMap(accInstrument->getInstrument(recurrentTransaction.getBudgetKey()))
                .flatMap(budgetInstrument->validateBudgetInstrument(budgetInstrument))
                .flatMap(budgetInstrument-> Mono.just(recurrentTransaction));
        }
        if(recurrentTransaction.getTransactionType().equals(TransactionType.TRANSFER)){
            return getInstrument(recurrentTransaction.getAccKey())
                .flatMap(accInstrument->validateCashInstrument(accInstrument))
                .flatMap(accInstrument->getInstrument(recurrentTransaction.getTrgAccKey()))
                .flatMap(trgAccInstrument->validateCashInstrument(trgAccInstrument))
                .flatMap(trgAccInstrument-> Mono.just(recurrentTransaction));
        }
        if(recurrentTransaction.getTransactionType().equals(TransactionType.BUDGETTRANSFER)){
            return getInstrument(recurrentTransaction.getTrgBudgetKey())
                .flatMap(trgBudgetInstrument->validateBudgetInstrument(trgBudgetInstrument))
                .flatMap(trgBudgetInstrument->getInstrument(recurrentTransaction.getBudgetKey()))
                .flatMap(budgetInstrument->validateBudgetInstrument(budgetInstrument))
                .flatMap(budgetInstrument-> Mono.just(recurrentTransaction));
        }
        if(recurrentTransaction.getTransactionType().equals(TransactionType.LIFEINSURANCEEXPENSE)){
            return getInstrument(recurrentTransaction.getAccKey())
                .flatMap(accInstrument->validateCashInstrument(accInstrument))
                .flatMap(accInstrument->getInstrument(recurrentTransaction.getBudgetKey()))
                .flatMap(budgetInstrument->validateBudgetInstrument(budgetInstrument))
                .flatMap(budgetInstrument->getInstrument(recurrentTransaction.getInsuranceKey()))
                .flatMap(lifeinsurance->validateLifeInsuranceInstrument(lifeinsurance))
                .flatMap(lifeinsurance-> Mono.just(recurrentTransaction));
        }
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "wrong recurrenttransactiontype"));
    }

    private Mono<String> recurrentTransactionApproved(RecurrentTransaction recurrentTransaction) {
        var msg = "recurrentTransaction validated: "+recurrentTransaction;
        auditService.saveMessage(msg, Severity.INFO, AUDIT_MSG_TYPE);
        recurrentTransactionApprovedEventHandler.sendRecurrentTransactionApprovedEvent(recurrentTransaction);
        return Mono.just(msg);
    }

    private Mono<Instrument> getInstrument(String instrumentId) {
        return dataReader.findByBusinesskey(instrumentId)
                .switchIfEmpty(handleNotExistingInstrument());
    }

    private Mono<Instrument> handleNotExistingInstrument(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "Not all necessary instruments available for this transaction:"));
    }

    private Mono<Instrument> validateCashInstrument(Instrument instrument){
        if(!instrument.getInstrumentType().equals(InstrumentType.GIRO)
            && !instrument.getInstrumentType().equals(InstrumentType.BUILDINGSAVINGACCOUNT)
            && !instrument.getInstrumentType().equals(InstrumentType.LOAN)
            && !instrument.getInstrumentType().equals(InstrumentType.MONEYATCALL)){
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttype for "+this.getClass().getName());
        }
        return Mono.just(instrument);
    }

    private Mono<Instrument> validateBudgetInstrument(Instrument instrument){
        if(!instrument.getInstrumentType().equals(InstrumentType.BUDGET)){
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttype for "+this.getClass().getName());
        }
        return Mono.just(instrument);
    }

    private Mono<Instrument> validateLifeInsuranceInstrument(Instrument instrument){
        if(!instrument.getInstrumentType().equals(InstrumentType.LIFEINSURANCE)){
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttype for "+this.getClass().getName());
        }
        return Mono.just(instrument);
    }

    private Mono<RecurrentTransaction> validateFrequency(RecurrentTransaction recurrentTransaction){
        if(recurrentTransaction.getRecurrentFrequency() == RecurrentFrequency.UNKNOWN) {
            return auditService.handleMonoError("no valid frequency", AUDIT_MSG_TYPE, MFMsgKey.NO_VALID_RECURRENTTRANSACTION).cast(RecurrentTransaction.class);
        }
        return Mono.just(recurrentTransaction);
    }

    private Mono<RecurrentTransaction> validateNextTransactionDate(RecurrentTransaction recurrentTransaction){
        if(recurrentTransaction.getNextTransactionDate()==null) {
            return auditService.handleMonoError("nextTransactionDate is in set", AUDIT_MSG_TYPE, MFMsgKey.NO_VALID_RECURRENTTRANSACTION).cast(RecurrentTransaction.class);
        }
        return Mono.just(recurrentTransaction);
    }

    private Mono<RecurrentTransaction> validateRecurrentTransactionId(RecurrentTransaction recurrentTransaction){
        if(recurrentTransaction.getRecurrentTransactionId()!=null && recurrentTransaction.getRecurrentTransactionId().trim().isEmpty()) {
            recurrentTransaction.setRecurrentTransactionId(null);
        }
        return Mono.just(recurrentTransaction);
    }

    public Flux<RecurrentTransaction> listRecurrentTransactions() {
        return dataReader.findRecurrentTransactions();
    }
}
