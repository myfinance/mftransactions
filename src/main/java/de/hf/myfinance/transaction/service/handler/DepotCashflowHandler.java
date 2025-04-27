package de.hf.myfinance.transaction.service.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    protected HashMap<String, List<InstrumentType>> setValidInstrumentTypeMap(Transaction transaction) {
        var instrumentKeyTypeMap = super.setValidInstrumentTypeMap(transaction);

        var validSecurityType = new ArrayList<InstrumentType>();
        validSecurityType.add(InstrumentType.BOND);
        validSecurityType.add(InstrumentType.EQUITY);
        validSecurityType.add(InstrumentType.FONDS);
        validSecurityType.add(InstrumentType.ETF);
        instrumentKeyTypeMap.put(transaction.getSecurityBusinessKey(), validSecurityType);
        return instrumentKeyTypeMap;
    }
}
