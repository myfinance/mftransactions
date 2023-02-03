package de.hf.myfinance.transaction.events.out;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static de.hf.myfinance.event.Event.Type.CREATE;
import static de.hf.myfinance.event.Event.Type.DELETE;

@Component
public class RecurrentTransactionApprovedEventHandler {

    private final StreamBridge streamBridge;

    public RecurrentTransactionApprovedEventHandler(StreamBridge streamBridge){
        this.streamBridge = streamBridge;
    }

    public void sendRecurrentTransactionApprovedEvent(RecurrentTransaction recurrentTransaction){
        sendMessage("recurrentTransactionaAproved-out-0",
                new Event<>(CREATE, "transactionPartition", recurrentTransaction));
    }

    public void sendDeleteRecurrentEvent(RecurrentTransaction recurrentTransaction){
        sendMessage("recurrentTransactionaAproved-out-0",
                new Event<>(DELETE, "transactionPartition", recurrentTransaction));
    }

    private void sendMessage(String bindingName, Event<String, RecurrentTransaction> event) {
        Message<Event<String, RecurrentTransaction>> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}