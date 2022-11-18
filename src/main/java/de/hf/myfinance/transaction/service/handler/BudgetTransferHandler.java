package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

import java.util.List;

public class BudgetTransferHandler extends AbsTransactionHandler{

    public BudgetTransferHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    @Override
    protected void validateInstrumentTypes(List<Instrument> instruments) {
        instruments.forEach(i->{
            if(!i.getInstrumentType().equals(InstrumentType.BUDGET)) {
                throw new MFException(MFMsgKey.NO_VALID_INSTRUMENT, "Wrong instrumenttype for budgetTransfer:"+i);
            }
        });
    }
}
