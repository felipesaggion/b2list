package br.com.b2list.mapper;

import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.domain.entity.PaymentCondition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentConditionMapper {

    PaymentConditionMapper INSTANCE = Mappers.getMapper(PaymentConditionMapper.class);

    PaymentConditionDTO toDto(PaymentCondition paymentCondition);

    PaymentCondition toEntity(PaymentConditionDTO paymentConditionDTO);
}
