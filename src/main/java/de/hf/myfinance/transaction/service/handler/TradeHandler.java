package de.hf.myfinance.transaction.service.handler;

import java.util.Map;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

public class TradeHandler  extends IncomeExpensesHandler{

    public TradeHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected void validateCashflowValue(Map<String, Double> cashflows) {
        this.validateCashflowValueGeneric(cashflows, TransactionType.SELL, TransactionType.BUY);
    }

    @Override
    protected Mono<Transaction> additionalValidation(Transaction transaction){
        if(transaction.getTradeInfo().getAmount() <= 0 ) {
            return Mono.error(new MFException(MFMsgKey.NO_VALID_TRANSACTION, "Trade amount is to allowed to <=0 but is:"+transaction.getTradeInfo().getAmount()));
        }
        return depotValidation(transaction).flatMap(this::securityValidation);
    }

    private Mono<Transaction> depotValidation(Transaction transaction){
        return transactionEnvironment.getDataReader().findByBusinesskey(transaction.getTradeInfo().getDepotBusinessKey()).switchIfEmpty(handleNotExistingInstrument(transaction.getTradeInfo().getDepotBusinessKey()))
        .flatMap(i->{
            if (!i.isActive()){
                return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "the depot is not allowed to be inactive:"+transaction.getTradeInfo().getDepotBusinessKey()));
            }
            return Mono.just(transaction);
        });
    }

    private Mono<Instrument> handleNotExistingInstrument(String instrumentId){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "No Instrument for this id available:"+instrumentId));
    }

    private Mono<Transaction> securityValidation(Transaction transaction){
        return transactionEnvironment.getDataReader().findByBusinesskey(transaction.getTradeInfo().getSecurityBusinessKey()).switchIfEmpty(handleNotExistingInstrument(transaction.getTradeInfo().getSecurityBusinessKey()))
        .flatMap(i->{
            if (!i.isActive()){
                return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "the security is not allowed to be inactive:"+transaction.getTradeInfo().getSecurityBusinessKey()));
            }
            return Mono.just(transaction);
        });
    }

}



