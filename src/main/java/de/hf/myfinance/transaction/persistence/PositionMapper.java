package de.hf.myfinance.transaction.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import de.hf.myfinance.restmodel.Position;
import de.hf.myfinance.transaction.persistence.entities.PositionEntity;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PositionMapper {

    @Mapping(source = "positionKey.depotBusinessKey", target = "depotBusinessKey")
    @Mapping(source = "positionKey.securityBusinessKey", target = "securityBusinessKey")
    Position entityToApi(PositionEntity entity);

    @Mapping(source = "depotBusinessKey", target = "positionKey.depotBusinessKey")
    @Mapping(source = "securityBusinessKey", target = "positionKey.securityBusinessKey")
    PositionEntity apiToEntity(Position api);

    List<Position> entityListToApiList(List<PositionEntity> entity);

    List<PositionEntity> apiListToEntityList(List<Position> api);

    default Position createTransaction() {
        return new Position("","",0);
    }
}
