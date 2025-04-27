package de.hf.myfinance.transaction.service.handler;


import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BudgetTransferHandler extends AbsTransactionHandler{

    public BudgetTransferHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected Mono<Transaction> validateInstruments(Transaction transaction){
        var instrumentKeyTypeMap = new HashMap<String, List<InstrumentType>>();

        var validBudgetType = new ArrayList<InstrumentType>();
        validBudgetType.add(InstrumentType.BUDGET);
        instrumentKeyTypeMap.put(transaction.getBudgetKey(), validBudgetType);
        instrumentKeyTypeMap.put(transaction.getTrgBudgetKey(), validBudgetType);

        return validateInstrumentTypes(instrumentKeyTypeMap)
            .flatMap(s->Mono.just(transaction));
    }
}
