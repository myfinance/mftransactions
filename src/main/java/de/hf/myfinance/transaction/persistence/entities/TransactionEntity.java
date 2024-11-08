package de.hf.myfinance.transaction.persistence.entities;

import de.hf.myfinance.restmodel.Trade;
import de.hf.myfinance.restmodel.TransactionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
    private Trade tradeInfo;
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

    public Trade getTradeInfo() {
        return tradeInfo;
    }

    public void setTradeInfo(Trade tradeInfo) {
        this.tradeInfo = tradeInfo;
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