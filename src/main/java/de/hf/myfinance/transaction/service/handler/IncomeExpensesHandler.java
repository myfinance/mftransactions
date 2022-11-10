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

import java.util.List;
import java.util.Map;

public class IncomeExpensesHandler extends AbsTransactionHandler{

    Instrument budget = null;
    Instrument giro = null;

    public IncomeExpensesHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    protected Mono<String> validateCashflows(Map<String, Double> cashflows){
        if(cashflows==null || cashflows.isEmpty() || cashflows.size()!=2) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid cashflows:"+cashflows);
        }
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+cashflows);
        }
        return this.transactionEnvironment.getDataReader().findInstrumentByBusinesskeyIn(cashflows.keySet())
                //.switchIfEmpty(handleNotExistingInstrument())
                .collectList().flatMap(i->validateInstruments(i))
                .flatMap(this::saveTransaction);

    }

    private Flux<Instrument> handleNotExistingInstrument(){
        throw new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "No Instruments for this transaction available.");
    }

    private Mono<String> validateInstruments(List<Instrument> instruments){
        if(instruments.size()!=2){
            throw new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "Not all Instruments for this transaction available.");
        }

        var firstInstrument = instruments.get(0);
        setInstrument(instruments.get(0));
        setInstrument(instruments.get(1));
        if(giro==null || budget==null) {
            return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttypes for incomeExpenses"));
        }
        if(!giro.getTenantBusinesskey().equals(budget.getTenantBusinesskey())){
            return Mono.error(new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Instruments have not the same tenant for incomeExpenses"));
        }
        return Mono.just("valid Transaction");
    }

    private void setInstrument(Instrument instrument){
        if(instrument.getInstrumentType().equals(InstrumentType.BUDGET)){
            budget = instrument;
        } else if(instrument.getInstrumentType().equals(InstrumentType.GIRO)){
            giro = instrument;
        }
    }

}
