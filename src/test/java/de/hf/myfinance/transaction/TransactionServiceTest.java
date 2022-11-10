package de.hf.myfinance.transaction;

import de.hf.framework.exceptions.MFException;
import de.hf.myfinance.exception.MFMsgKey;
import de.hf.myfinance.restmodel.Instrument;
import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.restmodel.TransactionType;
import de.hf.myfinance.transaction.persistence.DataReaderImpl;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
public class TransactionServiceTest extends EventProcessorTestBase{
    @Autowired
    TransactionService transactionService;

    @Autowired
    DataReaderImpl dataReader;

    String tenantDesc = "aTest";
    String tenantKey = "aTest@6";
    String budgetPfdesc = "bgtPf_"+tenantDesc;
    String bgtGrpdesc = "bgtGrp_"+budgetPfdesc;
    String bgtdesc = "incomeBgt_"+bgtGrpdesc;
    String bgtKey = bgtdesc+"@10";
    String giroKey = "newGiro@1";

    @Test
    void createIncome() {
        var budget = new InstrumentEntity(bgtKey, InstrumentType.BUDGET, true);
        budget.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(budget).block();
        var giro = new InstrumentEntity(giroKey, InstrumentType.GIRO, true);
        giro.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(giro).block();

        var desc = "testeinkommen";
        LocalDate transactionDate = LocalDate.of(2022, 1, 1);
        var transaction = new Transaction(desc, transactionDate, TransactionType.INCOMEEXPENSES);
        var cashflows = new HashMap<String, Double>();
        cashflows.put(bgtKey, 100.0);
        cashflows.put(giroKey, 100.0);
        transaction.setCashflows(cashflows);
        //var test3 = dataReader.findInstrumentByBusinesskeyIn(cashflows.keySet()).collectList().block();
        //var test4 = dataReader.findInstrumentByBusinesskeyIn(cashflows.keySet()).collectList().flatMap(i->test(i)).block();
        //var test5 = dataReader.findInstrumentByBusinesskeyIn(cashflows.keySet()).collectList().switchIfEmpty(handleNotExistingInstrument()).block();
        transactionService.addTransaction(transaction).block();

        final List<String> messages = getMessages(bindingName);
        assertEquals(1, messages.size());
    }

    /*private Mono<List<Instrument>> handleNotExistingInstrument(){
        throw new MFException(MFMsgKey.UNKNOWN_INSTRUMENT_EXCEPTION, "No Instruments for this transaction available.");
    }

    private Mono<Instrument> test(List<Instrument> i){
        return Mono.just(i.get(0));
    }*/

}
