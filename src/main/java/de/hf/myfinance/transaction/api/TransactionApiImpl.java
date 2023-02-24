package de.hf.myfinance.transaction.api;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restapi.TransactionApi;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RestController;

import de.hf.framework.utils.ServiceUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.LocalDate;

import static de.hf.myfinance.event.Event.Type.*;

@RestController
public class TransactionApiImpl implements TransactionApi {
    ServiceUtil serviceUtil;
    TransactionService transactionService;

    @Value("${api.common.version}")
    String apiVersion;

    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;

    @Autowired
    public TransactionApiImpl(ServiceUtil serviceUtil, TransactionService transactionService, StreamBridge streamBridge, @Qualifier("publishEventScheduler") Scheduler publishEventScheduler) {
        this.serviceUtil = serviceUtil;
        this.transactionService = transactionService;
        this.streamBridge = streamBridge;
        this.publishEventScheduler = publishEventScheduler;
    }

    @Override
    public String index() {
        return "Hello my TransactionService version:"+apiVersion;
    }


    @Override
    public Mono<String> delRecurrentTransfer(String recurrentTransactionId) {
        //there is no need to validate a recurrentTransaction for deletion. You can allways do this as long as the recurrenttransaction exists.
        // The SaveRecurrentTransactionProcessor will check this. So you can directly send the Approve event
        return Mono.fromCallable(() -> {
            var recurrentTransaction = new RecurrentTransaction();
            recurrentTransaction.setRecurrentTransactionId(recurrentTransactionId);
            sendMessage("recurrentTransactionaAproved-out-0",
                    new Event<>(DELETE, recurrentTransactionId, recurrentTransaction));
            return "recurrentTransaction deleted:"+recurrentTransaction;
        }).subscribeOn(publishEventScheduler);
    }


    @Override
    public Mono<String> saveRecurrentTransaction(RecurrentTransaction recurrentTransaction) {
        return Mono.fromCallable(() -> {

            sendMessage("validateRecurrentTransactionRequest-out-0",
                    new Event<>(CREATE, recurrentTransaction.toString(), recurrentTransaction));
            return "recurrentTransaction saved:"+recurrentTransaction;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<String> saveTransaction(Transaction transaction) {
        return transactionService.validateTransaction(transaction);
    }

    @Override
    public Mono<String> delTransaction(String transactionId) {
        return Mono.just("not implemented yet");
    }

    @Override
    public Flux<Transaction> listTransactions(LocalDate startDate, LocalDate endDate) {
        return transactionService.listTransactions(startDate, endDate);
    }

    @Override
    public Flux<RecurrentTransaction> listRecurrentTransactions() {
        return transactionService.listRecurrentTransactions();
    }

    @Override
    public Mono<String> processRecurrentTransaction() {
        return Mono.fromCallable(() -> {

            sendMessage("processRecurrentTransactions-out-0",
                    new Event<>(START, "processRecurrentTransactions", null));
            return "process recurrent Transactions started:";
        }).subscribeOn(publishEventScheduler);
    }

    /**
     * Since the sendMessage() uses blocking code, when calling streamBridge,
     * it has to be executed on a thread provided by a dedicated scheduler, publishEventScheduler
     */
    private void sendMessage(String bindingName, Event<String, Object> event) {
        Message<Event<String, Object>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

}