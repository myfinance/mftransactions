package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Position;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.entities.PositionKey;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.repositories.PositionRepository;
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
    private final PositionRepository positionRepository;
    private final PositionMapper positonMapper;

    public DataReaderImpl(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper,
                          TransactionRepository transactionRepository, TransactionMapper transactionMapper,
                          RecurrentTransactionMapper recurrentTransactionMapper, RecurrentTransactionRepository recurrentTransactionRepository, 
                          PositionRepository positionRepository, PositionMapper positionMapper
                          ) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.recurrentTransactionMapper = recurrentTransactionMapper;
        this.recurrentTransactionRepository = recurrentTransactionRepository;
        this.positionRepository = positionRepository;
        this.positonMapper = positionMapper;
    }

    @Override
    public Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable) {
        return instrumentRepository.findByBusinesskeyIn(businesskeyIterable)
                .map(instrumentMapper::entityToApi);
    }

    @Override
    public Flux<Transaction> findTransactiondateBetween(LocalDate startDate, LocalDate endDate) {
        // reduce the startdate by 1 and add a day to the enddate to include start and enddate in the search
        return transactionRepository.findByTransactiondateBetween(startDate.plusDays(-1), endDate.plusDays(1))
                .map(transactionMapper::entityToApi);
    }

    @Override
    public Mono<Transaction> findTransactionById(String id) {
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


    @Override
    public Flux<RecurrentTransaction> findRecurrentTransactionsByInstrument(String businesskey){
        return recurrentTransactionRepository.findByFirstInstrumentBusinessKeyOrSecondInstrumentBusinessKey(businesskey, businesskey)
                .map(recurrentTransactionMapper::entityToApi);
    }

    @Override
    public Mono<Position> findPositonByKey(String depotBusinessKey, String securityBusinessKey) {
        var positionkey = new PositionKey(depotBusinessKey, securityBusinessKey);
        return positionRepository.findByPositionKey(positionkey).map(positonMapper::entityToApi);
    }

}
