package br.com.b2list.mapper;

import br.com.b2list.domain.dto.OrderDTO;
import br.com.b2list.domain.dto.OrderItemDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderDTO toDto(Order order);

    Order toEntity(OrderDTO orderDTO);

    OrderItemDTO toDto(OrderItem orderItem);

    OrderItem toEntity(OrderItemDTO orderItemDTO);
}
