package br.com.b2list.mapper;

import br.com.b2list.domain.dto.BuyerDTO;
import br.com.b2list.domain.entity.Buyer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BuyerMapper {

    BuyerMapper INSTANCE = Mappers.getMapper(BuyerMapper.class);

    BuyerDTO toDto(Buyer buyer);

    Buyer toEntity(BuyerDTO buyerDTO);
}
