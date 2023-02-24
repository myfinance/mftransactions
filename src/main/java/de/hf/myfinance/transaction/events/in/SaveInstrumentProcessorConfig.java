package de.hf.myfinance.transaction.events.in;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.transaction.events.out.RecurrentTransactionApprovedEventHandler;
import de.hf.myfinance.transaction.persistence.DataReader;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.InstrumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class SaveInstrumentProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SaveInstrumentProcessorConfig.class);

    private final InstrumentMapper instrumentMapper;
    private final InstrumentRepository instrumentRepository;
    private final RecurrentTransactionApprovedEventHandler recurrentTransactionApprovedEventHandler;
    private final DataReader dataReader;

    @Autowired
    public SaveInstrumentProcessorConfig(InstrumentMapper instrumentMapper, InstrumentRepository instrumentRepository, DataReader dataReader, RecurrentTransactionApprovedEventHandler recurrentTransactionApprovedEventHandler) {
        this.instrumentMapper = instrumentMapper;
        this.instrumentRepository = instrumentRepository;
        this.dataReader = dataReader;
        this.recurrentTransactionApprovedEventHandler = recurrentTransactionApprovedEventHandler;
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
                        if(!instrument.isActive()) {
                            deleteRecurrentTransactions4InactiveInstruments(instrument.getBusinesskey()).block();
                        }
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
        return instrumentEntity;
    }

    private Mono<String> deleteRecurrentTransactions4InactiveInstruments(String businessKey){
        return dataReader.findRecurrentTransactionsByInstrument(businessKey).collectList().flatMap(this::sendDeleteRecurrentTransactionRequest);
    }

    private Mono<String> sendDeleteRecurrentTransactionRequest(List<RecurrentTransaction> recurrentTransactions){
        recurrentTransactions.forEach(r->recurrentTransactionApprovedEventHandler.sendDeleteRecurrentEvent(r));
        return Mono.just("recurrentTransactions for inactive Instrument deleted");
    }

}