package de.hf.myfinance.transaction.events.out;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Transaction;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static de.hf.myfinance.event.Event.Type.CREATE;
import static de.hf.myfinance.event.Event.Type.DELETE;

@Component
public class TransactionApprovedEventHandler {

    private final StreamBridge streamBridge;

    public TransactionApprovedEventHandler(StreamBridge streamBridge){
        this.streamBridge = streamBridge;
    }

    public void sendTransactionApprovedEvent(Transaction transaction){
        sendMessage("transactionaAproved-out-0",
                new Event<>(CREATE, "transactionPartition", transaction));
    }

    public void sendDeleteTransactionEvent(Transaction transaction){
        sendMessage("transactionaAproved-out-0",
                new Event<>(DELETE, "transactionPartition", transaction));
    }

    private void sendMessage(String bindingName, Event<String, Transaction> event) {
        Message<Event<String, Transaction>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}
