package br.com.b2list.mapper;

import br.com.b2list.domain.dto.ProductPriceDTO;
import br.com.b2list.domain.entity.ProductPrice;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductPriceMapper {

    ProductPriceMapper INSTANCE = Mappers.getMapper(ProductPriceMapper.class);

    ProductPriceDTO toDto(ProductPrice buyer);

    ProductPrice toEntity(ProductPriceDTO buyerDTO);
}
