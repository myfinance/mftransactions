package de.hf.myfinance.transaction.events.in;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.service.InstrumentMapper;
import de.hf.myfinance.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class SaveInstrumentProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SaveInstrumentProcessorConfig.class);

    private final InstrumentMapper instrumentMapper;
    private final InstrumentRepository instrumentRepository;

    @Autowired
    public SaveInstrumentProcessorConfig(InstrumentMapper instrumentMapper, InstrumentRepository instrumentRepository) {
        this.instrumentMapper = instrumentMapper;
        this.instrumentRepository = instrumentRepository;
    }

    @Bean
    public Consumer<Event<String, Instrument>> saveInstrumentProcessor() {
        return event -> {
            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getEventType()) {

                case CREATE:
                    Instrument instrument = event.getData();
                    LOG.info("Create instrument with ID: {}", instrument.getBusinesskey());
                    if(instrument.getInstrumentType().equals(InstrumentType.BUDGET) || instrument.getInstrumentType().equals(InstrumentType.GIRO)){
                        var instrumentEntity = map2entity(instrument);
                        instrumentRepository.deleteByBusinesskey(instrumentEntity.getBusinesskey()).then(instrumentRepository.save(instrumentEntity)).block();
                    }
                    break;

                default:
                    String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE event";
                    LOG.warn(errorMessage);
            }

            LOG.info("Message processing done!");

        };
    }

    private InstrumentEntity map2entity(Instrument instrument) {
        var instrumentEntity = instrumentMapper.apiToEntity(instrument);
        /*if(instrument.getAdditionalLists().containsKey(AdditionalLists.CHILDS)){
            instrumentEntity.setChilds(instrument.getAdditionalLists().get(AdditionalLists.CHILDS));
        }*/
        return instrumentEntity;
    }
}