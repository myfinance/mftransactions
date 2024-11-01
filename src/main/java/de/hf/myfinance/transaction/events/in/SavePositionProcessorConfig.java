package de.hf.myfinance.transaction.events.in;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.hf.framework.audit.AuditService;
import de.hf.framework.audit.Severity;
import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.entities.PositionEntity;
import de.hf.myfinance.transaction.persistence.entities.PositionKey;
import de.hf.myfinance.transaction.persistence.repositories.PositionRepository;
import reactor.core.publisher.Mono;
import java.util.function.Consumer;

@Configuration
public class SavePositionProcessorConfig {

    private final AuditService auditService;
    private final PositionRepository positionRepository;
    protected static final String AUDIT_MSG_TYPE="SaveInstrumentProcessor_Event";

    public SavePositionProcessorConfig(AuditService auditService, PositionRepository positionRepository) {

        this.auditService = auditService;
        this.positionRepository = positionRepository;
    }

    @Bean
    public Consumer<Event<String, Transaction>> savePositionProcessor() {
        return event -> {
            auditService.saveMessage("Process message created at " + event.getEventCreatedAt(), Severity.DEBUG, AUDIT_MSG_TYPE);
            Transaction transaction = event.getData();
            var amountChange = 0.0;
            switch (event.getEventType()) {

                case CREATE:
                    if (transaction.getTransactionType() == TransactionType.BUY){
                        amountChange = transaction.getTradeInfo().getAmount();
                    } else if (transaction.getTransactionType() == TransactionType.SELL){
                        amountChange = transaction.getTradeInfo().getAmount() * (-1);
                    }
                    else {
                        String errorMessage = "Incorrect transactionType: " + transaction.getTransactionType();
                        auditService.saveMessage(errorMessage, Severity.ERROR, AUDIT_MSG_TYPE);
                        break;
                    }
                    updatePosition(transaction.getTradeInfo().getDepotBusinessKey(), transaction.getTradeInfo().getSecurityBusinessKey(), amountChange);
                    break;

                case DELETE:
                    // revert the transaction
                    if (transaction.getTransactionType() == TransactionType.BUY){
                        amountChange = transaction.getTradeInfo().getAmount() * (-1);
                    } else if (transaction.getTransactionType() == TransactionType.SELL){
                        amountChange = transaction.getTradeInfo().getAmount();
                    }
                    else {
                        String errorMessage = "Incorrect transactionType: " + transaction.getTransactionType();
                        auditService.saveMessage(errorMessage, Severity.ERROR, AUDIT_MSG_TYPE);
                        break;
                    }
                    updatePosition(transaction.getTradeInfo().getDepotBusinessKey(), transaction.getTradeInfo().getSecurityBusinessKey(), amountChange);
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                    auditService.saveMessage(errorMessage, Severity.ERROR, AUDIT_MSG_TYPE);
            }

            auditService.saveMessage("Message processing done!", Severity.DEBUG, AUDIT_MSG_TYPE);

        };
    }

    private void updatePosition(String depotId, String securityId, double amountChange){
        PositionKey positionKey = new PositionKey(depotId, securityId);
        positionRepository.findByPositionKey(positionKey).switchIfEmpty(Mono.just(new PositionEntity(depotId, securityId, 0)))
            .flatMap(p->{
                p.setAmount(p.getAmount()+amountChange);
                return positionRepository.save(p);
            }).block();
    }
}

