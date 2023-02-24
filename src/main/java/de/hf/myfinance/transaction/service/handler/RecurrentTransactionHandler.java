package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.*;
import de.hf.myfinance.transaction.events.out.RecurrentTransactionApprovedEventHandler;
import de.hf.myfinance.transaction.events.out.ValidateTransactionEventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Component
public class RecurrentTransactionHandler {

    private AuditService auditService;
    private static final String ERROR_MSG = "Recurrent Transaction not valid";
    private static final String AUDIT_MSG_TYPE="RecurrentTransaction_User_Event";
    private final RecurrentTransactionApprovedEventHandler recurrentTransactionApprovedEventHandler;

    private final ValidateTransactionEventHandler validateTransactionEventHandler;

    private DataReader dataReader;

    @Autowired
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
            case QUATERLY:
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
        var cashflows = new HashMap<String, Double>();
        cashflows.put(i.getFirstInstrumentBusinessKey(), i.getValue());
        if(i.getTransactionType().equals(TransactionType.INCOME) || i.getTransactionType().equals(TransactionType.EXPENSE)) {
            cashflows.put(i.getSecondInstrumentBusinessKey(), i.getValue());
        } else {
            cashflows.put(i.getSecondInstrumentBusinessKey(), i.getValue() * (-1));
        }
        transaction.setCashflows(cashflows);
        return transaction;
    }

    public Mono<String> validateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {

        return getInstrument(recurrentTransaction.getFirstInstrumentBusinessKey())
                .zipWith(getInstrument(recurrentTransaction.getSecondInstrumentBusinessKey()), (i1, i2) -> evaluateRecurrentTransactionType(i1, i2, recurrentTransaction))
                .flatMap(this::validateFrequency)
                .flatMap(this::validateNextTransactionDate)
                .flatMap(this::recurrentTransactionApproved);
    }

    private Mono<String> recurrentTransactionApproved(RecurrentTransaction recurrentTransaction) {
        var msg = "recurrentTransaction validated: "+recurrentTransaction;
        auditService.saveMessage(msg, Severity.INFO, AUDIT_MSG_TYPE);
        recurrentTransactionApprovedEventHandler.sendRecurrentTransactionApprovedEvent(recurrentTransaction);
        return Mono.just(msg);
    }

    private Mono<Instrument> getInstrument(String instrumentId) {
        return dataReader.findByBusinesskey(instrumentId)
                .switchIfEmpty(handleNotExistingInstrument(instrumentId));
    }

    private Mono<RecurrentTransaction> validateFrequency(RecurrentTransaction recurrentTransaction){
        if(recurrentTransaction.getRecurrentFrequency() == RecurrentFrequency.UNKNOWN) {
            return auditService.handleMonoError("no valid frequency", AUDIT_MSG_TYPE, MFMsgKey.NO_VALID_RECURRENTTRANSACTION).cast(RecurrentTransaction.class);
        }
        return Mono.just(recurrentTransaction);
    }

    private Mono<RecurrentTransaction> validateNextTransactionDate(RecurrentTransaction recurrentTransaction){
        if(!recurrentTransaction.getNextTransactionDate().isAfter(LocalDate.now())) {
            return auditService.handleMonoError("nextTransactionDate is in the past", AUDIT_MSG_TYPE, MFMsgKey.NO_VALID_RECURRENTTRANSACTION).cast(RecurrentTransaction.class);
        }
        return Mono.just(recurrentTransaction);
    }

    private Mono<Instrument> handleNotExistingInstrument(String businesskey){
        String errorMsg = ERROR_MSG+ " Instrument for businesskey:" +businesskey + " does not exists.";
        return auditService.handleMonoError(errorMsg, AUDIT_MSG_TYPE, MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION).cast(Instrument.class);
    }

    protected RecurrentTransaction evaluateRecurrentTransactionType(Instrument firstInstrument, Instrument secondInstrument, RecurrentTransaction recurrentTransaction) {
        final String NO_VALID_INSTRUMENTTYPE_MSG = "no valid instrumenttype:";
        if(firstInstrument.getInstrumentType() == InstrumentType.BUDGET) {
            if(secondInstrument.getInstrumentType() == InstrumentType.BUDGET) {
                recurrentTransaction.setTransactionType(TransactionType.BUDGETTRANSFER);
            } else if( secondInstrument.getInstrumentType().getTypeGroup() == InstrumentTypeGroup.CASHACCOUNT ) {
                recurrentTransaction.setTransactionType(getRecurrentTransactiontype(recurrentTransaction.getValue()));
            } else {
                auditService.throwException(NO_VALID_INSTRUMENTTYPE_MSG + secondInstrument.getInstrumentType() , AUDIT_MSG_TYPE, MFMsgKey.WRONG_INSTRUMENTTYPE_EXCEPTION);
            }
        } else if(firstInstrument.getInstrumentType().getTypeGroup() == InstrumentTypeGroup.CASHACCOUNT){
            if(secondInstrument.getInstrumentType() == InstrumentType.BUDGET) {
                if(firstInstrument.getInstrumentType()==InstrumentType.GIRO) {
                    recurrentTransaction.setTransactionType(getRecurrentTransactiontype(recurrentTransaction.getValue()));
                } else {
                    auditService.throwException(NO_VALID_INSTRUMENTTYPE_MSG + " IncomeExpense is only allowed for GIRO-Accounts:" + secondInstrument.getInstrumentType() , AUDIT_MSG_TYPE, MFMsgKey.WRONG_INSTRUMENTTYPE_EXCEPTION);
                }
            } else if(secondInstrument.getInstrumentType().getTypeGroup() == InstrumentTypeGroup.CASHACCOUNT) {
                recurrentTransaction.setTransactionType(TransactionType.TRANSFER);
            } else {
                auditService.throwException(NO_VALID_INSTRUMENTTYPE_MSG + secondInstrument.getInstrumentType() , AUDIT_MSG_TYPE, MFMsgKey.WRONG_INSTRUMENTTYPE_EXCEPTION);
            }
        } else {
            auditService.throwException(NO_VALID_INSTRUMENTTYPE_MSG + firstInstrument.getInstrumentType() , AUDIT_MSG_TYPE, MFMsgKey.WRONG_INSTRUMENTTYPE_EXCEPTION);
        }
        return recurrentTransaction;
    }

    private TransactionType getRecurrentTransactiontype(double value) {
        if(value <0) {
            return TransactionType.EXPENSE;
        } else {
            return TransactionType.INCOME;
        }
    }

    public Flux<RecurrentTransaction> listRecurrentTransactions() {
        return dataReader.findRecurrentTransactions();
    }
}
