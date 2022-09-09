package de.hf.myfinance.transaction.service;


import de.hf.myfinance.restmodel.AdditionalLists;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionService {
    private final InstrumentMapper instrumentMapper;
    private final InstrumentRepository instrumentRepository;

    @Autowired
    public TransactionService(InstrumentMapper instrumentMapper, InstrumentRepository instrumentRepository){
        this.instrumentMapper = instrumentMapper;
        this.instrumentRepository = instrumentRepository;
    }

    public void updateInstrument(Instrument instrument) {
        if(instrument.getInstrumentType().equals(InstrumentType.BUDGET) || instrument.getInstrumentType().equals(InstrumentType.GIRO)){
            var instrumentEntity = map2entity(instrument);
            instrumentRepository.deleteByBusinesskey(instrumentEntity.getBusinesskey()).then(instrumentRepository.save(instrumentEntity)).block();

        }

    }

    private InstrumentEntity map2entity(Instrument instrument) {
        var instrumentEntity = instrumentMapper.apiToEntity(instrument);
        if(instrument.getAdditionalLists().containsKey(AdditionalLists.CHILDS)){
            instrumentEntity.setChilds(instrument.getAdditionalLists().get(AdditionalLists.CHILDS));
        }
        return instrumentEntity;
    }
}
