package de.hf.myfinance.transaction.persistence;


import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.Transaction;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface DataReader {
    Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable);
    Flux<Transaction> findTransactiondateBetween(LocalDate startDate, LocalDate endDate);
}
