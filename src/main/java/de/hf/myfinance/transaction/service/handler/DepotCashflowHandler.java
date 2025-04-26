package de.hf.myfinance.transaction.service.handler;

import java.util.List;
import java.util.Map;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class DepotCashflowHandler extends IncomeExpensesHandler{

    Instrument security = null;

    public DepotCashflowHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected void validateCashflowNumber(Map<String, Double> cashflows) {
        if(cashflows ==null || cashflows.isEmpty() || cashflows.size()!=3) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid cashflows:"+ cashflows);
        }
    }

    protected void validateInstrumentNumber(List<Instrument> instruments) {
        if(instruments.size()!=3){
            throw new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "Not all Instruments for this transaction available.");
        }
    }

    @Override
    protected void validateInstrumentTypes(List<Instrument> instruments) {
        setInstrument(instruments.get(0));
        setInstrument(instruments.get(1));
        setInstrument(instruments.get(2));
        if(giro==null || budget==null || security==null) {
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttypes for "+this.getClass().getName());
        }
    }

    @Override
    protected void setInstrument(Instrument instrument){
        super.setInstrument(instrument);

        if(instrument.getInstrumentType().equals(InstrumentType.BOND)
            || instrument.getInstrumentType().equals(InstrumentType.EQUITY)
            || instrument.getInstrumentType().equals(InstrumentType.FONDS)
            || instrument.getInstrumentType().equals(InstrumentType.ETF)){
            security = instrument;
        } 
    }


    @Override
    protected void validateCashflowValue(Map<String, Double> cashflows) {
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1)) && !values.get(0).equals(values.get(2))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+ cashflows);
        }
        if(values.get(0) < 0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " negative values for depotcashflows not allowed:"+ cashflows);
        }
    }
}
