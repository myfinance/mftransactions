package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.repositories.RecurrentTransactionRepository;
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
    private final RecurrentTransactionMapper recurrentTransactionMapper;
    private final RecurrentTransactionRepository recurrentTransactionRepository;

    @Autowired
    public DataReaderImpl(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper,
                          TransactionRepository transactionRepository, TransactionMapper transactionMapper,
                          RecurrentTransactionMapper recurrentTransactionMapper, RecurrentTransactionRepository recurrentTransactionRepository) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.recurrentTransactionMapper = recurrentTransactionMapper;
        this.recurrentTransactionRepository = recurrentTransactionRepository;
    }

    @Override
    public Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable) {
        return instrumentRepository.findByBusinesskeyIn(businesskeyIterable)
                .map(instrumentMapper::entityToApi);
    }

    @Override
    public Flux<Transaction> findTransactiondateBetween(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByTransactiondateBetween(startDate, endDate)
                .map(transactionMapper::entityToApi);
    }

    @Override
    public Mono<Transaction> findTransactiondateById(String id) {
        return transactionRepository.findById(id).map(transactionMapper::entityToApi);
    }

    @Override
    public Mono<Instrument> findByBusinesskey(String businesskey){
        return instrumentRepository.findByBusinesskey(businesskey)
                .map(instrumentMapper::entityToApi);
    }


    @Override
    public Flux<RecurrentTransaction> findRecurrentTransactions(){
        return recurrentTransactionRepository.findAll()
                .map(recurrentTransactionMapper::entityToApi);
    }
}
