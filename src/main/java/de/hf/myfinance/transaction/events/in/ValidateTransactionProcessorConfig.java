package de.hf.myfinance.transaction.events.in;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.events.out.TransactionApprovedEventHandler;
import de.hf.myfinance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.function.Consumer;

@Configuration
public class ValidateTransactionProcessorConfig {
    private final AuditService auditService;
    protected static final String AUDIT_MSG_TYPE="ValidateTransactionProcessorConfig_Event";
    private final TransactionService transactionService;
    private final TransactionApprovedEventHandler eventHandler;

    @Autowired
    public ValidateTransactionProcessorConfig(AuditService auditService, TransactionService transactionService, TransactionApprovedEventHandler eventHandler) {
        this.transactionService = transactionService;
        this.auditService = auditService;
        this.eventHandler = eventHandler;
    }

    @Bean
    public Consumer<Event<String, Transaction>> validateTransactionProcessor() {
        return event -> {
            auditService.saveMessage("Process message in ValidateTransactionProcessorConfig created at:" + event.getEventCreatedAt(), Severity.DEBUG, AUDIT_MSG_TYPE);

            if (Objects.requireNonNull(event.getEventType()) == Event.Type.CREATE) {
                Transaction transaction = event.getData();
                transactionService.validateTransaction(transaction).block();
            } 
            if (Objects.requireNonNull(event.getEventType()) == Event.Type.DELETE) {
                String transactionId = event.getKey();
                var transaction = transactionService.getTransaction(transactionId).block();
                eventHandler.sendDeleteTransactionEvent(transaction);
            } else {
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                auditService.saveMessage(errorMessage, Severity.ERROR, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing in ValidateTransactionProcessorConfig done!", Severity.DEBUG, AUDIT_MSG_TYPE);

        };
    }
}
