package de.hf.myfinance.transaction.persistence;

import de.hf.myfinance.restmodel.RecurrentTransaction;
import de.hf.myfinance.transaction.persistence.entities.RecurrentTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecurrentTransactionMapper {
    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    RecurrentTransaction entityToApi(RecurrentTransactionEntity entity);

    RecurrentTransactionEntity apiToEntity(RecurrentTransaction api);

    List<RecurrentTransaction> entityListToApiList(List<RecurrentTransactionEntity> entity);

    List<RecurrentTransactionEntity> apiListToEntityList(List<RecurrentTransaction> api);

    default RecurrentTransaction createTransaction() {
        return new RecurrentTransaction();
    }
}
