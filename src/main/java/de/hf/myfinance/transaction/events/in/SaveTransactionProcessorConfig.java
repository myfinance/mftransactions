package de.hf.myfinance.transaction.events.in;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.TransactionMapper;
import de.hf.myfinance.transaction.persistence.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class SaveTransactionProcessorConfig {

    private final TransactionMapper transactionMapper;
    private final AuditService auditService;
    private final TransactionRepository transactionRepository;
    protected static final String AUDIT_MSG_TYPE="SaveTransactionProcessor_Event";

    @Autowired
    public SaveTransactionProcessorConfig(TransactionMapper transactionMapper, AuditService auditService, TransactionRepository transactionRepository) {
        this.transactionMapper = transactionMapper;
        this.auditService = auditService;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Consumer<Event<String, Transaction>> saveTransactionProcessor() {
        return event -> {
            auditService.saveMessage("Process message created at "+ event.getEventCreatedAt(), Severity.INFO, AUDIT_MSG_TYPE);
            Transaction transaction = event.getData();

            switch (event.getEventType()) {

                case CREATE:
                    var transactionEntity = transactionMapper.apiToEntity(transaction);
                    transactionRepository.save(transactionEntity).block();
                    auditService.saveMessage("transaction saved:"+ transaction , Severity.INFO, AUDIT_MSG_TYPE);

                    break;

                case DELETE:
                    transactionRepository.deleteById(transaction.getTransactionId()).block();
                    auditService.saveMessage("transaction deleted:"+ transaction , Severity.INFO, AUDIT_MSG_TYPE);

                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                    auditService.saveMessage(errorMessage , Severity.ERROR, AUDIT_MSG_TYPE);
            }
        };
    }
}
