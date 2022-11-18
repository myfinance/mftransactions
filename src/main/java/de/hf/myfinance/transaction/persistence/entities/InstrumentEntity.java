package de.hf.myfinance.transaction.persistence.entities;

import de.hf.myfinance.restmodel.InstrumentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "instruments")
public class InstrumentEntity implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String instrumentid;
    @Version
    private Integer version;
    private InstrumentType instrumentType;
    private boolean isactive;
    @Indexed(unique = true)
    private String businesskey;

    private String tenantBusinesskey;


    public InstrumentEntity() {
    }

    public InstrumentEntity(String businesskey, InstrumentType instrumentType, boolean isactive) {
        setInstrumentType(instrumentType);
        this.businesskey = businesskey;
        this.isactive = isactive;
    }

    public String getInstrumentid() {
        return this.instrumentid;
    }
    public void setInstrumentid(String instrumentid) {
        this.instrumentid = instrumentid;
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