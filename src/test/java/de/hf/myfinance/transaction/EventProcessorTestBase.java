package de.hf.myfinance.transaction;

import de.hf.myfinance.restmodel.InstrumentType;
import de.hf.myfinance.transaction.persistence.entities.InstrumentEntity;
import de.hf.myfinance.transaction.persistence.repositories.InstrumentRepository;
import de.hf.myfinance.transaction.persistence.repositories.RecurrentTransactionRepository;
import de.hf.myfinance.transaction.persistence.repositories.TransactionRepository;
import de.hf.testhelper.MongoDbTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Import({TestChannelBinderConfiguration.class})
public class EventProcessorTestBase extends MongoDbTestBase {
    @Autowired
    InstrumentRepository instrumentRepository;
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    RecurrentTransactionRepository recurrentTransactionRepository;

    @Autowired
    private OutputDestination target;

    String bindingName = "transactionaAproved-out-0";

    String tenantDesc = "aTest";
    String tenantKey = "aTest@6";
    String otherTenantKey = "anotherTest@6";
    String budgetPfdesc = "bgtPf_"+tenantDesc;
    String bgtGrpdesc = "bgtGrp_"+budgetPfdesc;
    String bgtdesc = "incomeBgt_"+bgtGrpdesc;
    String bgtKey = bgtdesc+"@10";
    String giroKey = "newGiro@1";
    String bgt2desc = "someBgt_"+bgtGrpdesc;
    String bgt2Key = bgt2desc+"@10";
    String giro2Key = "newGiro2@1";
    String giroOtherTenantKey = "newOtherTenantGiro2@1";

    @BeforeEach
    void setupDb() {
        instrumentRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
        recurrentTransactionRepository.deleteAll().block();
        purgeMessages(bindingName);
    }

    protected void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    protected List<String> getMessages(String bindingName){
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message =
                    getMessage(bindingName);

            if (message == null) {
                anyMoreMessages = false;

            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    protected Message<byte[]> getMessage(String bindingName){
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }

    protected void initDb() {
        var budget = new InstrumentEntity(bgtKey, InstrumentType.BUDGET, true);
        budget.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(budget).block();
        var giro = new InstrumentEntity(giroKey, InstrumentType.GIRO, true);
        giro.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(giro).block();

        var budget2 = new InstrumentEntity(bgt2Key, InstrumentType.BUDGET, true);
        budget2.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(budget2).block();
        var giro2 = new InstrumentEntity(giro2Key, InstrumentType.GIRO, true);
        giro2.setTenantBusinesskey(tenantKey);
        instrumentRepository.save(giro2).block();

        var girootherTenant = new InstrumentEntity(giroOtherTenantKey, InstrumentType.GIRO, true);
        girootherTenant.setTenantBusinesskey(otherTenantKey);
        instrumentRepository.save(girootherTenant).block();
    }
}
