package de.hf.myfinance.transaction.service;

import de.hf.framework.audit.AuditService;
import de.hf.myfinance.transaction.events.out.TransactionApprovedEventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import org.springframework.stereotype.Component;

@Component
public record TransactionEnvironment(DataReader dataReader,
                                     AuditService auditService,
                                     TransactionApprovedEventHandler eventHandler) {

    public DataReader getDataReader() {
        return dataReader;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public TransactionApprovedEventHandler getEventHandler() {
        return eventHandler;
    }
}
