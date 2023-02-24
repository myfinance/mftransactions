package de.hf.myfinance.transaction.persistence;


import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.restmodel.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface DataReader {
    Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable);
    Flux<Transaction> findTransactiondateBetween(LocalDate startDate, LocalDate endDate);
    Mono<Transaction> findTransactiondateById(String id);
    Mono<Instrument> findByBusinesskey(String businesskey);
    Flux<RecurrentTransaction> findRecurrentTransactions();
    Flux<RecurrentTransaction> findRecurrentTransactionsByInstrument(String businesskey);
}
