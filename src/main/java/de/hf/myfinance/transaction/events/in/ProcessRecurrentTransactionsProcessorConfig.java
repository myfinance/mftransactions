package de.hf.myfinance.transaction.events.in;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.transaction.persistence.InstrumentMapper;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;

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
            auditService.saveMessage("Process message created at "+ event.getEventCreatedAt(), Severity.INFO, AUDIT_MSG_TYPE);

            switch (event.getEventType()) {

                case START:
                    transactionService.processRecurrentTransactions().block();
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a Start event";
                    auditService.saveMessage(errorMessage, Severity.WARN, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing done!", Severity.INFO, AUDIT_MSG_TYPE);

        };
    }

}
