package de.hf.myfinance.transaction.service.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class LifeInsurenceExpenseHandler  extends IncomeExpensesHandler{

    Instrument security = null;

    public LifeInsurenceExpenseHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected HashMap<String, List<InstrumentType>> setValidInstrumentTypeMap(Transaction transaction) {
        var instrumentKeyTypeMap = super.setValidInstrumentTypeMap(transaction);

        var validInstrumentType = new ArrayList<InstrumentType>();
        validInstrumentType.add(InstrumentType.LIFEINSURANCE);
        instrumentKeyTypeMap.put(transaction.getInsuranceKey(), validInstrumentType);
        return instrumentKeyTypeMap;
    }
}