package de.hf.myfinance.transaction.api;

import de.hf.myfinance.restapi.TransactionApi;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Trade;
import de.hf.myfinance.restmodel.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import de.hf.framework.utils.ServiceUtil;

@RestController
public class TransactionApiImpl implements TransactionApi {
    ServiceUtil serviceUtil;

    @Autowired
    public TransactionApiImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public String index() {
        return "Hello my TransactionService";
    }

    @Override
    public void addTrade(Trade trade) {

    }

    @Override
    public void updateTrade(Trade trade) {

    }

    @Override
    public void delRecurrentTransfer(String recurrentTransactionId) {

    }

    @Override
    public void delTransfer(String transactionId) {

    }

    @Override
    public void updateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {

    }

    @Override
    public void addRecurrentTransaction(RecurrentTransaction recurrentTransaction) {

    }

    @Override
    public void addTransaction(Transaction transaction) {

    }

    @Override
    public void updateTransaction(Transaction transaction) {

    }

}