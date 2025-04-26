package de.hf.myfinance.transaction.service.handler;

import java.util.Map;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class InterestHandler extends IncomeExpensesHandler{

    public InterestHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }
    @Override
    protected void validateCashflowValue(Map<String, Double> cashflows) {
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1))) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+ cashflows);
        }
        if(values.get(0) < 0) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " negative values for interests not allowed:"+ cashflows);
        }
    }

}
