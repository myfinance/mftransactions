package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

import java.util.List;
import java.util.Map;

public class IncomeExpensesHandler extends AbsTransactionHandler{

    Instrument budget = null;
    Instrument giro = null;

    public IncomeExpensesHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected void validateCashflowValue(Map<String, Double> cashflows) {
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+ cashflows);
        }
        if(transaction.getTransactionType().equals(TransactionType.INCOME) && values.get(0) < 0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " negative values for income not allowed:"+ cashflows);
        }
        if(transaction.getTransactionType().equals(TransactionType.EXPENSE) && values.get(0) > 0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " positive values for expense not allowed:"+ cashflows);
        }
    }

    @Override
    protected void validateInstrumentTypes(List<Instrument> instruments) {
        setInstrument(instruments.get(0));
        setInstrument(instruments.get(1));
        if(giro==null || budget==null) {
            throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttypes for incomeExpenses");
        }
    }


    private void setInstrument(Instrument instrument){
        if(instrument.getInstrumentType().equals(InstrumentType.BUDGET)){
            budget = instrument;
        } else if(instrument.getInstrumentType().equals(InstrumentType.GIRO)){
            giro = instrument;
        }
    }

}
