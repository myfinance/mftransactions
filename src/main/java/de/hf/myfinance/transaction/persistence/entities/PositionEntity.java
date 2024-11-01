package de.hf.myfinance.transaction.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "positions")
public class PositionEntity {
    @Id
    private String positionId;
    @Version
    private Integer version;

    @Indexed(unique = true)
    private PositionKey positionKey;
    private double amount;

    public PositionEntity(){}

    public PositionEntity(String positionId) {
        this.positionId = positionId;
    }
    public PositionEntity(String depotBusinessKey, String securityBusinessKey, double amount) {
        this.positionKey = new PositionKey(depotBusinessKey,securityBusinessKey);
        this.amount = amount;
     }
  
     public double getAmount() {
        return this.amount;
     }
  
     public void setAmount(double amount) {
        this.amount = amount;
     }

     public PositionKey getPositionKey() {
        return this.positionKey;
     }
  
     public void setPositionKey(PositionKey positionKey) {
        this.positionKey = positionKey;
     }
}
