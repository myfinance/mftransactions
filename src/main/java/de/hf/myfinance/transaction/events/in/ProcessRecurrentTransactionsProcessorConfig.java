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
public class ProcessRecurrentTransactionsProcessorConfig {

    private final AuditService auditService;
    private final TransactionService transactionService;
    protected static final String AUDIT_MSG_TYPE="ProcessRecurrentTransactionsProcessorConfig_Event";
    @Autowired
    public ProcessRecurrentTransactionsProcessorConfig(AuditService auditService, TransactionService transactionService) {
        this.auditService = auditService;
        this.transactionService = transactionService;
    }

    @Bean
    public Consumer<Event<String, RecurrentTransaction>> processRecurrentTransactionsProcessor() {
        return event -> {
            auditService.saveMessage("Process message in ProcessRecurrentTransactionsProcessorConfig created at:" + event.getEventCreatedAt(), Severity.DEBUG, AUDIT_MSG_TYPE);

            switch (event.getEventType()) {

                case START:
                    transactionService.processRecurrentTransactions().block();
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a Start event";
                    auditService.saveMessage(errorMessage, Severity.FATAL, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing in ProcessRecurrentTransactionsProcessorConfig done!", Severity.DEBUG, AUDIT_MSG_TYPE);
        };
    }

}
