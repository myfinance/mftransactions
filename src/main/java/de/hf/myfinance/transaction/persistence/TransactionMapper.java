package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.Transaction;
import de.hf.myfinance.transaction.persistence.entities.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Transaction entityToApi(TransactionEntity entity);

    TransactionEntity apiToEntity(Transaction api);

    List<Transaction> entityListToApiList(List<TransactionEntity> entity);

    List<TransactionEntity> apiListToEntityList(List<Transaction> api);

    default Transaction createTransaction() {
        return new Transaction("");
    }
}
