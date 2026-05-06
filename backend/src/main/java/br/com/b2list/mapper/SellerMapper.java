package br.com.b2list.mapper;

import br.com.b2list.domain.dto.SellerDTO;
import br.com.b2list.domain.entity.Seller;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    SellerMapper INSTANCE = Mappers.getMapper(SellerMapper.class);

    SellerDTO toDto(Seller seller);

    Seller toEntity(SellerDTO sellerDTO);
}
