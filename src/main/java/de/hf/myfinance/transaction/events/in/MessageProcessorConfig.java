package de.hf.myfinance.transaction.events.in;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final TransactionService transactionService;

    @Autowired
    public MessageProcessorConfig(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Bean
    public Consumer<Event<Integer, Instrument>> messageProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {

                case CREATE:
                    Instrument instrument = event.getData();
                    LOG.info("Create instrument with ID: {}", instrument.getBusinesskey());
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                    LOG.warn(errorMessage);
            }

            LOG.info("Message processing done!");

        };
    }
}