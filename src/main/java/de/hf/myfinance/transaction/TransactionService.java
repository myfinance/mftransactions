package de.hf.myfinance.transaction;


import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import org.springframework.stereotype.Component;

@Component
public class TransactionService {

    public void updateInstrument(Instrument instrument) {
        if(instrument.getInstrumentType().equals(InstrumentType.BUDGET) || instrument.getInstrumentType().equals(InstrumentType.GIRO)){

        }

    }
}
