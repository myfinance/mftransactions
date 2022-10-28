package de.hf.myfinance.transaction.service;

import de.hf.framework.audit.AuditService;
import de.hf.myfinance.transaction.events.out.EventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;

public class TransactionEnvironment {
    private final DataReader dataReader;
    private final AuditService auditService;
    private final EventHandler eventHandler;

    public TransactionEnvironment(DataReader dataReader, AuditService auditService, EventHandler eventHandler) {
        this.dataReader = dataReader;
        this.auditService = auditService;
        this.eventHandler = eventHandler;
    }

    public DataReader getDataReader() {
        return dataReader;
    }

    public AuditService getAuditService() {
        return auditService;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }
}
