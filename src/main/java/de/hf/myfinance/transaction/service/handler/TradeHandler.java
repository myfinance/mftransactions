package de.hf.myfinance.transaction.service.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class TradeHandler extends DepotCashflowHandler{

    Instrument security = null;

    public TradeHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected HashMap<String, List<InstrumentType>> setValidInstrumentTypeMap(Transaction transaction) {
        var instrumentKeyTypeMap = super.setValidInstrumentTypeMap(transaction);

        var validDepotType = new ArrayList<InstrumentType>();
        validDepotType.add(InstrumentType.DEPOT);
        instrumentKeyTypeMap.put(transaction.getDepotBusinessKey(), validDepotType);
        return instrumentKeyTypeMap;
    }
}



