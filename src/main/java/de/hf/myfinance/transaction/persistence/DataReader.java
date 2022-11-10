package de.hf.myfinance.transaction.persistence;


import de.hf.myfinance.restmodel.Instrument;
import reactor.core.publisher.Flux;

public interface DataReader {
    Flux<Instrument> findInstrumentByBusinesskeyIn(Iterable<String> businesskeyIterable);
}
