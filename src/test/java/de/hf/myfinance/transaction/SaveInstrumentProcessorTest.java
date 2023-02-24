package de.hf.myfinance.transaction;

import de.hf.myfinance.event.Event;
import de.hf.myfinance.restmodel.*;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import de.hf.testhelper.JsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
public class SaveInstrumentProcessorTest extends EventProcessorTestBase {

    @Autowired
    @Qualifier("saveInstrumentProcessor")
    protected Consumer<Event<String, Instrument>> saveInstrumentProcessor;

    @Test
    void saveInstrument() {
        var budget = new Instrument(bgtKey, "thebudget", InstrumentType.BUDGET, true);
        budget.setTenantBusinesskey(tenantKey);
        Event creatEvent = new Event(Event.Type.CREATE, bgtKey, budget);
        saveInstrumentProcessor.accept(creatEvent);

        var instruments = instrumentRepository.findAll().collectList().block();
        assertEquals(1, instruments.size());
        var instrument = instruments.get(0);
        assertNotNull(instrument.getInstrumentid());
        assertEquals(bgtKey, instrument.getBusinesskey());
        assertTrue(instrument.isActive());
        assertEquals(tenantKey, instrument.getTenantBusinesskey());
        assertEquals(InstrumentType.BUDGET, instrument.getInstrumentType());

        var giro = new Instrument(giroKey, "theGiro", InstrumentType.GIRO, true);
        giro.setTenantBusinesskey(tenantKey);
        creatEvent = new Event(Event.Type.CREATE, giroKey, giro);
        saveInstrumentProcessor.accept(creatEvent);

        instruments = instrumentRepository.findAll().collectList().block();
        assertEquals(2, instruments.size());
    }

    @Test
    void inactivateInstrument() {
        var budget = new Instrument(bgtKey, "thebudget", InstrumentType.BUDGET, true);
        budget.setTenantBusinesskey(tenantKey);
        Event creatEvent = new Event(Event.Type.CREATE, bgtKey, budget);
        saveInstrumentProcessor.accept(creatEvent);

        var instruments = instrumentRepository.findAll().collectList().block();
        assertEquals(1, instruments.size());
        var instrument = instruments.get(0);
        assertNotNull(instrument.getInstrumentid());
        assertEquals(bgtKey, instrument.getBusinesskey());
        assertTrue(instrument.isActive());
        assertEquals(tenantKey, instrument.getTenantBusinesskey());
        assertEquals(InstrumentType.BUDGET, instrument.getInstrumentType());

        var giro = new Instrument(giroKey, "theGiro", InstrumentType.GIRO, true);
        giro.setTenantBusinesskey(tenantKey);
        creatEvent = new Event(Event.Type.CREATE, giroKey, giro);
        saveInstrumentProcessor.accept(creatEvent);

        instruments = instrumentRepository.findAll().collectList().block();
        assertEquals(2, instruments.size());


        var nextTransactiondate = LocalDate.now().minusDays(1);
        var recurrentTransaction = new RecurrentTransactionEntity();
        recurrentTransaction.setRecurrentFrequency(RecurrentFrequency.MONTHLY);
        recurrentTransaction.setNextTransactionDate(nextTransactiondate);
        recurrentTransaction.setFirstInstrumentBusinessKey(giroKey);
        recurrentTransaction.setSecondInstrumentBusinessKey(bgtKey);
        recurrentTransaction.setDescription("test");
        recurrentTransaction.setValue(100);
        recurrentTransaction.setTransactionType(TransactionType.INCOME);
        recurrentTransactionRepository.save(recurrentTransaction).block();

        giro.setActive(false);
        creatEvent = new Event(Event.Type.CREATE, giroKey, giro);
        saveInstrumentProcessor.accept(creatEvent);

        instrument = instrumentRepository.findByBusinesskey(giroKey).block();
        assertEquals(giroKey, instrument.getBusinesskey());
        assertFalse(instrument.isActive());

        final List<String> recurrenttransactionUpdateMessages = getMessages(recurrentTransactionApprovedBindingName);
        assertEquals(1, recurrenttransactionUpdateMessages.size());
        JsonHelper jsonHelper = new JsonHelper();
        var msg = jsonHelper.convertJsonStringToMap((recurrenttransactionUpdateMessages.get(0)));
        assertEquals("DELETE", (String)msg.get("eventType"));
        var data = (LinkedHashMap)msg.get("data");
        assertEquals(giroKey, data.get("firstInstrumentBusinessKey"));
        assertEquals(bgtKey, data.get("secondInstrumentBusinessKey"));
        assertEquals(RecurrentFrequency.MONTHLY.toString(), data.get("recurrentFrequency"));
        assertEquals(nextTransactiondate.toString(), data.get("nextTransactionDate"));
        assertEquals(100.0, data.get("value"));
        assertEquals("test", data.get("description"));
        assertEquals(TransactionType.INCOME.toString(), data.get("transactionType"));

    }
}
