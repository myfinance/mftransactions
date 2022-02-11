package de.hf.myfinance.transaction.api;

import de.hf.myfinance.restapi.TransactionApi;
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

}