package de.hf.myfinance.transaction.events.in;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ValidateRecurrentTransactionProcessorConfig {

    private final TransactionService transactionService;
    private final AuditService auditService;
    private static final String AUDIT_MSG_TYPE="ValidateRecurrentTransactionProcessorConfig_Event";

    @Autowired
    public ValidateRecurrentTransactionProcessorConfig(TransactionService transactionService, AuditService auditService) {
        this.transactionService = transactionService;
        this.auditService = auditService;
    }

    @Bean
    public Consumer<Event<String, RecurrentTransaction>> validateRecurrentTransactionProcessor() {
        return event -> {
            auditService.saveMessage("Process message in ValidateRecurrentTransactionProcessorConfig created at:" + event.getEventCreatedAt(), Severity.DEBUG, AUDIT_MSG_TYPE);

            if (event.getEventType() == Event.Type.CREATE) {
                RecurrentTransaction recurrentTransaction = event.getData();
                transactionService.validateRecurrentTransaction(recurrentTransaction).block();
            } else {
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                auditService.saveMessage(errorMessage, Severity.ERROR, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing in ValidateRecurrentTransactionProcessorConfig done!", Severity.DEBUG, AUDIT_MSG_TYPE);

        };
    }
}