package de.hf.myfinance.transaction.service.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.InstrumentTypeGroup;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;


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

    protected Mono<String> validateTenant(List<Instrument> instruments) {

        var filteredInstruments = instruments.stream().filter(i->!i.getInstrumentType().getTypeGroup().equals(InstrumentTypeGroup.SECURITY)).toList();
        return super.validateTenant(filteredInstruments);
    }
}
