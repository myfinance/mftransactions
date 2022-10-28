package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.repositories.TransactionRepository;
import de.hf.myfinance.transaction.service.InstrumentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DataReaderImpl implements DataReader{
    private final InstrumentRepository instrumentRepository;
    private final InstrumentMapper instrumentMapper;
    private final TransactionRepository transactionRepository;

    @Autowired
    public DataReaderImpl(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper, TransactionRepository transactionRepository) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Flux<Instrument> findByBusinesskeyIn(Iterable<String> businesskeyIterable) {
        return instrumentRepository.findByBusinesskeyIn(businesskeyIterable).map(e-> instrumentMapper.entityToApi(e));
    }
}
