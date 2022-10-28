package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

import java.util.Map;

public class IncomeExpensesHandler extends AbsTransactionHandler{

    public IncomeExpensesHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

    protected void validateCashflows(Map<String, Double> cashflows){
        if(cashflows==null || cashflows.isEmpty() || cashflows.size()!=2) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " no valid cashflows:"+cashflows);
        }
        var values = cashflows.values().stream().toList();
        if(!values.get(0).equals(values.get(1)) {
            throw new MFException(MFMsgKey.NO_VALID_TRANSACTION, " value of cashflows not equal:"+cashflows);
        }
        var instruments = this.transactionEnvironment.getDataReader().findByBusinesskeyIn(cashflows.keySet());
        //immer checken ob empty
        //tenant vergleichen
        //check dass ein typ budget und einer giro ist
    }


}
