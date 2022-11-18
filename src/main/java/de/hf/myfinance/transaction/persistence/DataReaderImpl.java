package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
public class DataReaderImpl implements DataReader{
    private final InstrumentRepository instrumentRepository;
    private final InstrumentMapper instrumentMapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Autowired
    public DataReaderImpl(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper, TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable) {
        return instrumentRepository.findByBusinesskeyIn(businesskeyIterable)
                .map(e->
                        instrumentMapper.entityToApi(e)
                );
    }

    @Override
    public Flux<Transaction> findTransactiondateBetween(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactiondateBetween(startDate, endDate)
                .map(e->
                        transactionMapper.entityToApi(e)
                );
    }

    @Override
    public Mono<Transaction> findTransactiondateById(String id) {
        return transactionRepository.findById(id).map(e->transactionMapper.entityToApi(e));
    }
}
