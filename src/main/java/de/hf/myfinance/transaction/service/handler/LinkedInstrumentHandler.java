package de.hf.myfinance.transaction.service.handler;

import java.util.List;

import de.hf.framework.audit.AuditService;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

public class LinkedInstrumentHandler extends IncomeExpensesHandler{


    public LinkedInstrumentHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected Mono<Transaction> additionalValidation(Transaction transaction){
        return super.additionalValidation(transaction)           
            .flatMap(this::validateAccount)
            .flatMap(this::validateSecurity);
    }

    protected Mono<Transaction> validateAccount(Transaction transaction){
        return this.transactionEnvironment.getDataReader().findByBusinesskey(transaction.getAccId())
            .switchIfEmpty(handleNotExistingInstrument(transaction.getAccId()))
            .flatMap(this::validateAccountType)
            .flatMap(i->{return Mono.just(transaction);});
    }

    protected Mono<Transaction> validateSecurity(Transaction transaction){
        if(transaction.getTransactionType() == TransactionType.INTERESTS) {
            return Mono.just(transaction);
        }
        return this.transactionEnvironment.getDataReader().findByBusinesskey(transaction.getSecurityId())
            .switchIfEmpty(handleNotExistingInstrument(transaction.getSecurityId()))
            .flatMap(this::validateSecurityType)
            .flatMap(i->{return Mono.just(transaction);});
    }

    private Mono<Instrument> handleNotExistingInstrument(String id){
        return transactionEnvironment.getAuditService().handleMonoError("instrument for businesskey:"+id + " does not exists.", AUDIT_MSG_TYPE, MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION).cast(Instrument.class);
    }

    protected Mono<Transaction> validateSecurityType(Instrument instrument) {
        if(instrument.getInstrumentType() != InstrumentType.BOND
            && instrument.getInstrumentType() != InstrumentType.EQUITY
            && instrument.getInstrumentType() != InstrumentType.ETF
            && instrument.getInstrumentType() != InstrumentType.FONDS)
            {
            return transactionEnvironment.getAuditService().handleMonoError("instrument:"+instrument.getBusinesskey() + " has the wrong type.", AUDIT_MSG_TYPE, MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION).cast(Transaction.class);
        }
        return Mono.just(transaction);
    }

    protected Mono<Transaction> validateAccountType(Instrument instrument) {
        if(instrument.getInstrumentType() != InstrumentType.DEPOT
            && instrument.getInstrumentType() != InstrumentType.GIRO
            && instrument.getInstrumentType() != InstrumentType.LIFEINSURANCE
            && instrument.getInstrumentType() != InstrumentType.MONEYATCALL
            && instrument.getInstrumentType() != InstrumentType.TIMEDEPOSIT)
        {
            return transactionEnvironment.getAuditService().handleMonoError("instrument:"+instrument.getBusinesskey() + " has the wrong type.", AUDIT_MSG_TYPE, MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION).cast(Transaction.class);
        }
        if(transaction.getTransactionType() == TransactionType.DEPOTCASHFLOW
            && instrument.getInstrumentType() != InstrumentType.DEPOT)
        {
            return transactionEnvironment.getAuditService().handleMonoError("instrument:"+instrument.getBusinesskey() + " has the wrong type.", AUDIT_MSG_TYPE, MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION).cast(Transaction.class);
        }
        return Mono.just(transaction);
    }
    
}
