package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.AuditService;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.events.out.EventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionHandlerFactory {
    private final TransactionEnvironment transactionEnvironment;

    @Autowired
    public TransactionHandlerFactory(DataReader dataReader, AuditService auditService, EventHandler eventHandler) {
        transactionEnvironment = new TransactionEnvironment(dataReader, auditService, eventHandler);
    }

    public TransactionHandler createTransactionHandler(Transaction transaction) {
        switch(transaction.getTransactionType()){
            case INCOMEEXPENSES:
                return new IncomeExpensesHandler(transactionEnvironment, transaction);
            /*case LINKEDINCOMEEXPENSES:
                return new LinkedIncomeExpensesHandler(instrumentService, transactionDao, auditService, cashflowDao);
            case TRADE:
                return new TradeHandler(instrumentService, transactionDao, auditService, cashflowDao, tradeDao);
            case TRANSFER:
                return new TransferHandler(instrumentService, transactionDao, auditService, cashflowDao);
            case BUDGETTRANSFER:
                return new BudgetTransferHandler(instrumentService, transactionDao, auditService, cashflowDao);*/
            default:
                throw new MFException(MFMsgKey.UNKNOWN_TRNSACTIONTYPE_EXCEPTION, "can not create Transactionhandler for transactionType:"+transaction.getTransactionType());
        }
    }
}
