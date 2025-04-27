package de.hf.myfinance.transaction.service.handler;

import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionEnvironment;

public class InterestHandler extends IncomeExpensesHandler{

    public InterestHandler(TransactionEnvironment transactionEnvironment, Transaction transaction) {
        super(transactionEnvironment, transaction);
    }

}
