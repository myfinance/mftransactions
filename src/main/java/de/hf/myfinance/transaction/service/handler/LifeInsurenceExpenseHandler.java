package de.hf.myfinance.transaction.service.handler;

import java.util.List;
import java.util.Map;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class LifeInsurenceExpenseHandler extends DepotCashflowHandler{

    Instrument lifeinsurance = null;

    public LifeInsurenceExpenseHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected void validateInstrumentTypes(List<Instrument> instruments) {
        setInstrument(instruments.get(0));
        setInstrument(instruments.get(1));
        setInstrument(instruments.get(2));
        if(giro==null || budget==null || lifeinsurance==null) {
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttypes for "+this.getClass().getName());
        }
    }

    @Override
    protected void setInstrument(Instrument instrument){
        super.setInstrument(instrument);

        if(instrument.getInstrumentType().equals(InstrumentType.LIFEINSURANCE)){
            lifeinsurance = instrument;
        } 
    }


    @Override
    protected void validateCashflowValue(Map<String, Double> cashflows) {
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1)) && !values.get(0).equals(values.get(2))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+ cashflows);
        }
        if(values.get(0) > 0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " positive values for lifeinsuranceExpenses not allowed:"+ cashflows);
        }
    }
}