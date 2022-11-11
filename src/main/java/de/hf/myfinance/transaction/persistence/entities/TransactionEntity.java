package de.hf.myfinance.transaction.persistence.entities;

import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Trade;
import de.hf.myfinance.restmodel.TransactionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Document(collection = "transactions")
public class TransactionEntity  implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String transactionId;
    @Version
    private Integer version;

    private String description;
    private LocalDate transactiondate;
    private LocalDateTime lastchanged;
    private Set<Trade> trades = new HashSet<Trade>(0);
    private Map<String, Double> cashflows = new HashMap<>(0);
    private TransactionType transactionType;

    public TransactionEntity(){}

    public TransactionEntity(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionEntity(String description, LocalDate transactiondate, TransactionType transactionType) {
        this.description = description;
        this.transactiondate = transactiondate;
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return this.transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactiondate() {
        return transactiondate;
    }

    public void setTransactiondate(LocalDate transactiondate) {
        this.transactiondate = transactiondate;
    }

    public LocalDateTime getLastchanged() {
        return lastchanged;
    }

    public void setLastchanged(LocalDateTime lastchanged) {
        this.lastchanged = lastchanged;
    }

    public Set<Trade> getTrades() {
        return trades;
    }

    public void setTrades(Set<Trade> trades) {
        this.trades = trades;
    }

    public Map<String, Double> getCashflows() {
        return cashflows;
    }

    public void setCashflows(Map<String, Double> cashflows) {
        this.cashflows = cashflows;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}