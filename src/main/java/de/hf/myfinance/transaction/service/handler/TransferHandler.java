package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TransferHandler  extends AbsTransactionHandler{

    public TransferHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected Mono<Transaction> validateInstruments(Transaction transaction){
        var instrumentKeyTypeMap = new HashMap<String, List<InstrumentType>>();
        var validAccType = new ArrayList<InstrumentType>();
        validAccType.add(InstrumentType.GIRO);
        validAccType.add(InstrumentType.BUILDINGSAVINGACCOUNT);
        validAccType.add(InstrumentType.LOAN);
        validAccType.add(InstrumentType.MONEYATCALL);
        validAccType.add(InstrumentType.TIMEDEPOSIT);
        instrumentKeyTypeMap.put(transaction.getAccKey(), validAccType);
        instrumentKeyTypeMap.put(transaction.getTrgAccKey(), validAccType);

        return validateInstrumentTypes(instrumentKeyTypeMap)
            .flatMap(s->Mono.just(transaction));
    }
}