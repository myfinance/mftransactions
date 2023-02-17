package de.hf.myfinance.transaction.events.in;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.function.Consumer;

@Configuration
public class ValidateTransactionProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ValidateTransactionProcessorConfig.class);

    private final TransactionService transactionService;

    @Autowired
    public ValidateTransactionProcessorConfig(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Bean
    public Consumer<Event<String, Transaction>> validateTransactionProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            if (Objects.requireNonNull(event.getEventType()) == Event.Type.CREATE) {
                Transaction transaction = event.getData();
                transactionService.validateTransaction(transaction).block();
            } else {
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                LOG.warn(errorMessage);
            }

            LOG.info("Message processing done!");

        };
    }
}
