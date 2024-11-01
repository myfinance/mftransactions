package de.hf.myfinance.transaction.service.handler;

import java.util.Map;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Position;
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
        return depotValidation(transaction).flatMap(this::securityValidation).flatMap(this::positionValidation);
    }

    private Mono<Transaction> depotValidation(Transaction transaction){
        return transactionEnvironment.getDataReader().findByBusinesskey(transaction.getTradeInfo().getDepotBusinessKey()).switchIfEmpty(handleNotExistingInstrument())
        .flatMap(i->{
            if (!i.isActive()){
                return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "the depot is not allowed to be inactive:"+transaction.getTradeInfo().getDepotBusinessKey()));
            }
            return Mono.just(transaction);
        });
    }

    private Mono<Instrument> handleNotExistingInstrument(){
        return Mono.error(new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "No Instrument for this id available:"+transaction.getTradeInfo().getDepotBusinessKey()));
    }

    private Mono<Transaction> securityValidation(Transaction transaction){
        return transactionEnvironment.getDataReader().findByBusinesskey(transaction.getTradeInfo().getSecurityBusinessKey()).switchIfEmpty(handleNotExistingInstrument())
        .flatMap(i->{
            if (!i.isActive()){
                return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "the security is not allowed to be inactive:"+transaction.getTradeInfo().getSecurityBusinessKey()));
            }
            return Mono.just(transaction);
        });
    }

    private Mono<Transaction> positionValidation(Transaction transaction){
        if(transaction.getTransactionType()==TransactionType.SELL){
            return transactionEnvironment.getDataReader().findPositonByKey(transaction.getTradeInfo().getDepotBusinessKey(),transaction.getTradeInfo().getSecurityBusinessKey()).switchIfEmpty(handleNotExistingPosition())
            .flatMap(p->{
                if (!(p.getAmount() >= transaction.getTradeInfo().getAmount())){
                    return Mono.error(new MFException(MFMsgKey.NO_VALID_TRANSACTION, "the positionamount is not high anough so sell so many:"+transaction.getTradeInfo().getAmount()));
                }
                return Mono.just(transaction);
            });
        }
        return Mono.just(transaction);
    }

    private Mono<Position> handleNotExistingPosition(){
        return Mono.error(new MFException(MFMsgKey.NO_VALID_TRANSACTION, "No Position for this Equity id :"+transaction.getTradeInfo().getSecurityBusinessKey()));
    }

}



