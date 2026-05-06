package br.com.b2list.mapper;

import br.com.b2list.domain.dto.WarehouseDTO;
import br.com.b2list.domain.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    WarehouseMapper INSTANCE = Mappers.getMapper(WarehouseMapper.class);

    WarehouseDTO toDto(Warehouse warehouse);

    Warehouse toEntity(WarehouseDTO warehouseDTO);
}
