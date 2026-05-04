package br.com.b2list.controller;


import br.com.b2list.domain.dto.OrderPageResponseDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.projection.OrderListingProjection;
import br.com.b2list.service.OrderService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.create(orderRequestDTO);
    }

    @GetMapping
    public ResponseEntity<OrderPageResponseDTO<OrderListingProjection>> findAllPaginated(
            @RequestParam int size,
            @RequestParam int page,
            @RequestParam OrderStatus status,
            @RequestParam String buyerRef,
            @RequestParam OffsetDateTime startDate,
            @RequestParam OffsetDateTime endDate) {
        return ResponseEntity.ok(orderService.listPaginated(
                        startDate,
                        endDate,
                        status,
                        buyerRef,
                        TenantContext.getTenant(),
                        Pageable.ofSize(size).withPage(page)
                )
        );
    }
}
