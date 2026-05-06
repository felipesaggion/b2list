package br.com.b2list.service;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.StatisticsDTO;
import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.enums.OrderStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

public interface OrderService {
    ResponseEntity<?> create(OrderRequestDTO orderRequestDTO);

    Order save(Order buyer);

    String generateCode();

    ResponseEntity<?> listPaginated(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            OrderStatus status,
            String buyerRef,
            String tenantCode,
            Integer page,
            Integer size);

    ResponseEntity<?> cancelOrder(String externalReference);

    ResponseEntity<?> findByExternatReference(String externalReference);

    StatisticsDTO generateReport(String tenant, OffsetDateTime from, OffsetDateTime to);

    ResponseEntity<ErrorResponseDTO> decreaseBuyersLimit(Order orderSaved, Buyer buyer);
}
