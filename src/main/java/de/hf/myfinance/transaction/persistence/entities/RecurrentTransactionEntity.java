package de.hf.myfinance.transaction.persistence.entities;

import de.hf.myfinance.restmodel.RecurrentFrequency;
import de.hf.myfinance.restmodel.TransactionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "recurrentTransactions")
public class RecurrentTransactionEntity {
    @Id
    private String recurrentTransactionId;
    @Version
    private Integer version;
    private String firstInstrumentBusinessKey;
    private String secondInstrumentBusinessKey;
    private RecurrentFrequency recurrentFrequency;
    private double value;
    private LocalDate nextTransactionDate;
    private TransactionType transactionType;
    private String description;

    public String getRecurrentTransactionId() {
        return this.recurrentTransactionId;
    }

    public void setRecurrentTransactionId(String recurrentTransactionId) {
        this.recurrentTransactionId = recurrentTransactionId;
    }

    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFirstInstrumentBusinessKey() {
        return this.firstInstrumentBusinessKey;
    }

    public void setFirstInstrumentBusinessKey(String firstInstrumentBusinessKey) {
        this.firstInstrumentBusinessKey = firstInstrumentBusinessKey;
    }

    public String getSecondInstrumentBusinessKey() {
        return this.secondInstrumentBusinessKey;
    }

    public void setSecondInstrumentBusinessKey(String secondInstrumentBusinessKey) {
        this.secondInstrumentBusinessKey = secondInstrumentBusinessKey;
    }

    public RecurrentFrequency getRecurrentFrequency() {
        return this.recurrentFrequency;
    }

    public void setRecurrentFrequency(RecurrentFrequency recurrentFrequency) {
        this.recurrentFrequency = recurrentFrequency;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDate getNextTransactionDate() {
        return this.nextTransactionDate;
    }

    public void setNextTransactionDate(LocalDate nextTransactionDate) {
        this.nextTransactionDate = nextTransactionDate;
    }

    public TransactionType getTransactionType() {
        return this.transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
