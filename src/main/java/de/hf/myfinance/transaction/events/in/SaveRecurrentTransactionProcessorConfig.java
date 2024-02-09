package de.hf.myfinance.transaction.events.in;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.transaction.persistence.RecurrentTransactionMapper;
import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import de.hf.myfinance.transaction.persistence.repositories.RecurrentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
public class SaveRecurrentTransactionProcessorConfig {

    protected static final String AUDIT_MSG_TYPE="SaveRecurrentTransactionProcessor_Event";
    private final RecurrentTransactionMapper recurrentTransactionMapper;
    private final AuditService auditService;
    private final RecurrentTransactionRepository recurrentTransactionRepository;

    @Autowired
    public SaveRecurrentTransactionProcessorConfig(RecurrentTransactionMapper recurrentTransactionMapper, AuditService auditService, RecurrentTransactionRepository recurrentTransactionRepository) {
        this.recurrentTransactionMapper = recurrentTransactionMapper;
        this.auditService = auditService;
        this.recurrentTransactionRepository = recurrentTransactionRepository;
    }

    @Bean
    public Consumer<Event<String, RecurrentTransaction>> saveRecurrentTransactionProcessor() {
        return event -> {
            auditService.saveMessage("Process message in SaveRecurrentTransactionProcessorConfig created at:" + event.getEventCreatedAt(), Severity.DEBUG, AUDIT_MSG_TYPE);

            if (event.getEventType() == Event.Type.CREATE) {
                var recurrentTransactionEntity = recurrentTransactionMapper.apiToEntity(event.getData());

                if(recurrentTransactionEntity.getRecurrentTransactionId()==null) {
                    recurrentTransactionRepository.save(recurrentTransactionEntity).block();
                    auditService.saveMessage("recurrentTransaction inserted:" + recurrentTransactionEntity, Severity.INFO, AUDIT_MSG_TYPE);
                } else {
                    recurrentTransactionRepository.findById(recurrentTransactionEntity.getRecurrentTransactionId())
                            .switchIfEmpty(Mono.error(new MFException(MFMsgKey.NO_VALID_RECURRENTTRANSACTION, " id not found:"+ recurrentTransactionEntity.getRecurrentTransactionId())))
                            .map(e -> {
                                    e.setNextTransactionDate(recurrentTransactionEntity.getNextTransactionDate());
                                    e.setTransactionType(recurrentTransactionEntity.getTransactionType());
                                    e.setFirstInstrumentBusinessKey(recurrentTransactionEntity.getFirstInstrumentBusinessKey());
                                    e.setSecondInstrumentBusinessKey(recurrentTransactionEntity.getSecondInstrumentBusinessKey());
                                    e.setValue(recurrentTransactionEntity.getValue());
                                    e.setRecurrentFrequency(recurrentTransactionEntity.getRecurrentFrequency());
                                    return e;
                                })
                            .flatMap(recurrentTransactionRepository::save)
                            .block();
                    auditService.saveMessage("recurrentTransaction updated:" + recurrentTransactionEntity, Severity.INFO, AUDIT_MSG_TYPE);
                }
            } if (event.getEventType() == Event.Type.DELETE) {
                var recurrentTransactionEntity = recurrentTransactionMapper.apiToEntity(event.getData());
                if(recurrentTransactionEntity.getRecurrentTransactionId()==null || recurrentTransactionEntity.getRecurrentTransactionId().isEmpty()) {
                    auditService.throwException("RecurrenttransactionID is empty.", AUDIT_MSG_TYPE, MFMsgKey.ILLEGAL_ARGUMENTS);
                } else {
                    recurrentTransactionRepository.findById(recurrentTransactionEntity.getRecurrentTransactionId())
                            .switchIfEmpty(auditService.handleMonoError("Recurrenttransaction not found for id:"+recurrentTransactionEntity.getRecurrentTransactionId(), AUDIT_MSG_TYPE, MFMsgKey.NO_VALID_RECURRENTTRANSACTION).cast(RecurrentTransactionEntity.class))
                            .flatMap(recurrentTransactionRepository::delete)
                            .block();
                    auditService.saveMessage("recurrentTransaction updated:" + recurrentTransactionEntity, Severity.INFO, AUDIT_MSG_TYPE);
                }
            } else {
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                auditService.saveMessage(errorMessage, Severity.WARN, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing in SaveRecurrentTransactionProcessorConfig done!", Severity.DEBUG, AUDIT_MSG_TYPE);
        };
    }
}
