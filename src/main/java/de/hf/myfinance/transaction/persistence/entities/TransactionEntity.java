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


    private InstrumentType instrumentType;
    private boolean isactive;
    @Indexed(unique = true)
    private String businesskey;

    private String tenantBusinesskey;

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

    public InstrumentType getInstrumentType(){
        return instrumentType;
    }
    public void setInstrumentType(InstrumentType instrumentType) {
        this.instrumentType = instrumentType;
    }

    public boolean isIsactive() {
        return this.isactive;
    }
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }


    public String getTenantBusinesskey() {
        return this.tenantBusinesskey;
    }
    public void setTenantBusinesskey(String tenantBusinesskey) {
        this.tenantBusinesskey = tenantBusinesskey;
    }

    public String getBusinesskey() {
        return this.businesskey;
    }
    public void setBusinesskey(String businesskey) {
        this.businesskey = businesskey;
    }


    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }

}