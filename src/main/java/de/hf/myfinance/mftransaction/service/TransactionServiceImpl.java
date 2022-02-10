package de.hf.myfinance.mftransaction.service;

import de.hf.myfinance.restapi.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import de.hf.framework.utils.ServiceUtil;

@RestController
public class TransactionServiceImpl implements TransactionService {
    ServiceUtil serviceUtil;

    @Autowired
    public TransactionServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public String index() {
        return "Hello my TransactionService";
    }

}