package br.com.b2list.controller;


import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.StatisticsDTO;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.service.OrderService;
import br.com.b2list.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<?> findAllPaginated(
            @RequestParam Integer page,
            @RequestParam Integer size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String buyerRef,
            @RequestParam OffsetDateTime startDate,
            @RequestParam OffsetDateTime endDate) {

        return orderService.listPaginated(
                startDate,
                endDate,
                status,
                buyerRef,
                TenantContext.getTenant(),
                page,
                size
        );
    }

    @GetMapping("/{externalReference}")
    public ResponseEntity<?> findByExternalReference(@PathVariable String externalReference) {
        return orderService.findByExternatReference(externalReference);
    }

    @PostMapping("/{externalReference}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String externalReference) {
        return orderService.cancelOrder(externalReference);
    }

    @GetMapping("/statistics")
    public StatisticsDTO getStatisticsReport(@RequestParam OffsetDateTime from, @RequestParam OffsetDateTime to) {
        return orderService.generateReport(TenantContext.getTenant(), from, to);
    }

}
