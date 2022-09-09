package de.hf.myfinance.transaction.persistence.entities;

import de.hf.myfinance.restmodel.AdditionalMaps;
import de.hf.myfinance.restmodel.AdditionalProperties;
import de.hf.myfinance.restmodel.InstrumentType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "instruments")
public class InstrumentEntity implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String instrumentid;
    @Version
    private Integer version;
    private Integer instrumentTypeId;
    private InstrumentType instrumentType;
    private boolean isactive;
    @Indexed(unique = true)
    private String businesskey;

    private String tenant;
    private List<String> childs;


    public InstrumentEntity() {
    }

    public InstrumentEntity(InstrumentType instrumentType, boolean isactive) {
        setInstrumentTypeId(instrumentType.getValue());
        this.isactive = isactive;
    }

    public String getInstrumentid() {
        return this.instrumentid;
    }
    public void setInstrumentid(String instrumentid) {
        this.instrumentid = instrumentid;
    }

    protected Integer getInstrumentTypeId() {
        return this.instrumentTypeId;
    }
    protected void setInstrumentTypeId(Integer instrumentTypeId) {
        this.instrumentTypeId = instrumentTypeId;
        instrumentType = InstrumentType.getInstrumentTypeById(instrumentTypeId);
    }

    public InstrumentType getInstrumentType(){
        return instrumentType;
    }


    public boolean isIsactive() {
        return this.isactive;
    }
    public void setIsactive(boolean isactive) {
        this.isactive = isactive;
    }


    public String getTenant() {
        return this.tenant;
    }
    public void setTenant(String tenant) {
        this.tenant = tenant;
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

    public List<String> getChilds() {
        return childs;
    }
    public void setChilds(List<String> childs) {
        this.childs = childs;
    }
}