package de.hf.myfinance.transaction.events.out;

import de.hf.myfinance.event.Event;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static de.hf.myfinance.event.Event.Type.CREATE;

@Component
public class EventHandler {

    private final StreamBridge streamBridge;

    public EventHandler(StreamBridge streamBridge){
        this.streamBridge = streamBridge;
    }

    public void sendInstrumentUpdatedEvent(){
        /*var instrument = instrumentMapper.entityToApi(instrumentEntity);
        sendMessage("instrumentupdates-out-0",
                new Event(CREATE, instrument.getBusinesskey().hashCode(), instrument));*/
    }

    private void sendMessage(String bindingName, Event event) {
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }
}
