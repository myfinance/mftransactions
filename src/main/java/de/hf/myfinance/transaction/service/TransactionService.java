package de.hf.myfinance.transaction.service;

import de.hf.myfinance.restmodel.Cashflow;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.handler.RecurrentTransactionHandler;
import de.hf.myfinance.transaction.service.handler.TransactionHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
public class TransactionService {

    private final TransactionHandlerFactory transactionHandlerFactory;
    private final RecurrentTransactionHandler recurrentTransactionHandler;

    public TransactionService(TransactionHandlerFactory transactionHandlerFactory, RecurrentTransactionHandler recurrentTransactionHandler){
        this.transactionHandlerFactory = transactionHandlerFactory;
        this.recurrentTransactionHandler = recurrentTransactionHandler;
    }

    public Mono<String> validateRecurrentTransaction(RecurrentTransaction recurrentTransaction) {
        return recurrentTransactionHandler.validateRecurrentTransaction(recurrentTransaction);
    }

    public Mono<Transaction> validateTransaction(Transaction transaction) {
        return transactionHandlerFactory.createTransactionHandler(transaction).validate();
    }

    public Flux<Transaction> listTransactions(LocalDate startDate, LocalDate endDate) {
        return transactionHandlerFactory.listTransactions(startDate, endDate);
    }

    public Flux<Cashflow> listInstrumentCashflows(String businesskey, LocalDate startDate, LocalDate endDate) {

        return transactionHandlerFactory.listTransactions(startDate, endDate)
            .flatMap(i->this.filterTransactionsAndConvert2Cashflows(businesskey,i)).filter(c->c.getValue()!=0);

    }

    private Mono<Cashflow> filterTransactionsAndConvert2Cashflows(String businesskey, Transaction transactions){
        var cashflow = new Cashflow();
        cashflow.setValue(0);
        if(transactions.getCashflows().containsKey(businesskey)){
            cashflow.setDescription(transactions.getDescription());
            cashflow.setInstrumentBusinesskey(businesskey);
            cashflow.setTransactiondate(transactions.getTransactiondate());
            cashflow.setValue(transactions.getCashflows().get(businesskey));
        }
        return Mono.just(cashflow);
    }

    public Mono<Transaction> getTransaction(String transactionId) {
        return transactionHandlerFactory.getTransaction(transactionId);
    }

    public Flux<RecurrentTransaction> listRecurrentTransactions() {
        return recurrentTransactionHandler.listRecurrentTransactions();
    }

    public Mono<String> processRecurrentTransactions(){
        return recurrentTransactionHandler.process();
    }

    public Mono<Double> getAvgExpensesOfLastYear(String businesskey){
        var endDate = LocalDate.of(
            LocalDate.now().getYear(), 
            LocalDate.now().minusMonths(1).getMonth(), 
            1);
        return listInstrumentCashflows(businesskey, endDate.minusYears(1), endDate).filter(c->c.getValue()<0).map(Cashflow::getValue).reduce(0.0,Double::sum).map(s->s/12);
    }
}
