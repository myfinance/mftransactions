package de.hf.myfinance.transaction.service.handler;

import de.hf.framework.audit.AuditService;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.events.out.TransactionApprovedEventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import de.hf.myfinance.transaction.service.TransactionEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
public class TransactionHandlerFactory {
    private final TransactionEnvironment transactionEnvironment;

    @Autowired
    public TransactionHandlerFactory(DataReader dataReader, AuditService auditService, TransactionApprovedEventHandler eventHandler) {
        transactionEnvironment = new TransactionEnvironment(dataReader, auditService, eventHandler);
    }

    public TransactionHandler createTransactionHandler(Transaction transaction) {
        switch(transaction.getTransactionType()){
            case INCOMEEXPENSES:
                return new IncomeExpensesHandler(transactionEnvironment, transaction);
            case TRANSFER:
                return new TransferHandler(transactionEnvironment, transaction);
            case BUDGETTRANSFER:
                return new BudgetTransferHandler(transactionEnvironment, transaction);
            default:
                throw new MFException(MFMsgKey.UNKNOWN_TRNSACTIONTYPE_EXCEPTION, "can not create Transactionhandler for transactionType:"+transaction.getTransactionType());
        }
    }

    public Flux<Transaction> listTransactions(LocalDate startDate, LocalDate endDate) {
        return transactionEnvironment.getDataReader().findTransactiondateBetween(startDate, endDate);
    }
}
