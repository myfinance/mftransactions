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
    private String budgetKey;
    private String trgBudgetKey;
    private String accKey;
    private String trgAccKey;
    private String insuranceKey; 
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


    public String getBudgetKey() {
        return this.budgetKey;
    }

    public void setBudgetKey(String budgetKey) {
        this.budgetKey = budgetKey;
    }

    public String getTrgBudgetKey() {
        return this.trgBudgetKey;
    }

    public void setTrgBudgetKey(String trgBudgetKey) {
        this.trgBudgetKey = trgBudgetKey;
    }

    public String getAccKey() {
        return this.accKey;
    }

    public void setAccKey(String accKey) {
        this.accKey = accKey;
    }

    public String getTrgAccKey() {
        return this.trgAccKey;
    }

    public void setTrgAccKey(String trgAccKey) {
        this.trgAccKey = trgAccKey;
    }

    public String getInsuranceKey() {
        return this.insuranceKey;
    }

    public void setInsuranceKey(String insuranceKey) {
        this.insuranceKey = insuranceKey;
    }

}
