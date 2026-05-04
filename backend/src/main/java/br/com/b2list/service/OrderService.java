package br.com.b2list.service;

import br.com.b2list.domain.dto.OrderPageResponseDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.projection.OrderListingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    ResponseEntity<?> create(OrderRequestDTO orderRequestDTO);
    Order save(Order buyer);
    List<Order> findAll();
    Order findById(UUID id);
    void deleteById(UUID id);
    String generateCode();
    OrderPageResponseDTO<OrderListingProjection> listPaginated(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            OrderStatus status,
            String buyerRef,
            String tenantCode,
            Pageable pageable);
}
