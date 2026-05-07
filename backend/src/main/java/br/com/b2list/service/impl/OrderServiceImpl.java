package br.com.b2list.service.impl;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.domain.dto.ItemDTO;
import br.com.b2list.domain.dto.OrderDTO;
import br.com.b2list.domain.dto.OrderItemDTO;
import br.com.b2list.domain.dto.OrderPageResponseDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.dto.PeriodDTO;
import br.com.b2list.domain.dto.StatisticsDTO;
import br.com.b2list.domain.dto.TopBuyerDTO;
import br.com.b2list.domain.dto.TopProductDTO;
import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.OrderItem;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.domain.entity.ProductPrice;
import br.com.b2list.domain.entity.Seller;
import br.com.b2list.domain.entity.Warehouse;
import br.com.b2list.enums.Error;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.event.OrderPayload;
import br.com.b2list.mapper.BuyerMapper;
import br.com.b2list.mapper.OrderMapper;
import br.com.b2list.mapper.PaymentConditionMapper;
import br.com.b2list.mapper.SellerMapper;
import br.com.b2list.mapper.WarehouseMapper;
import br.com.b2list.producer.OrderEventProducer;
import br.com.b2list.projection.OrderListingProjection;
import br.com.b2list.projection.OrderSummaryProjection;
import br.com.b2list.repository.OrderRepository;
import br.com.b2list.service.BuyerService;
import br.com.b2list.service.OrderService;
import br.com.b2list.service.PaymentConditionService;
import br.com.b2list.service.ProductPriceService;
import br.com.b2list.service.SellerService;
import br.com.b2list.service.WarehouseService;
import br.com.b2list.tenant.TenantContext;
import br.com.b2list.util.ErrorUtil;
import br.com.b2list.util.OrderUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static br.com.b2list.util.OrderUtils.checkIfOrderIsAlreadyCanceled;
import static br.com.b2list.util.OrderUtils.checkIfOrderIsPresent;
import static br.com.b2list.util.OrderUtils.checkIfTenantIsPresent;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private PaymentConditionService paymentConditionService;

    @Autowired
    private ProductPriceService productPriceService;

    @Autowired
    private OrderEventProducer orderEventProducer;

    @Autowired
    public OrderMapper orderMapper;

    @Autowired
    private BuyerMapper buyerMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private PaymentConditionMapper paymentConditionMapper;

    @Autowired
    private OrderRuleEngine orderRuleEngine;

    @Override
    @Transactional(rollbackOn = DataIntegrityViolationException.class)
    public ResponseEntity<?> create(OrderRequestDTO orderRequestDTO) {
        String tenant = TenantContext.getTenant();

        ResponseEntity<ErrorResponseDTO> errorResponse = checkIfTenantIsPresent(tenant);
        if (errorResponse != null) return errorResponse;
        errorResponse = OrderUtils.checkIfExternalReferenceIsPresent(orderRequestDTO);
        if (errorResponse != null) return errorResponse;
        errorResponse = OrderUtils.checkIfBuyerIsPresent(orderRequestDTO);
        if (errorResponse != null) return errorResponse;
        errorResponse = OrderUtils.checkIfSellerIsPresent(orderRequestDTO);
        if (errorResponse != null) return errorResponse;
        errorResponse = OrderUtils.checkIfWarehouseIsPresent(orderRequestDTO);
        if (errorResponse != null) return errorResponse;
        errorResponse = OrderUtils.checkIfPaymentConditionIsPresent(orderRequestDTO);
        if (errorResponse != null) return errorResponse;

        Buyer buyer = buyerService.findByExternalReferenceAndEnabledTrueAndTenantCode(
                orderRequestDTO.getBuyerReference(),
                tenant
        );
        errorResponse = OrderUtils.validateBuyer(orderRequestDTO, buyer);
        if (errorResponse != null) return errorResponse;

        Seller seller = sellerService.findByExternalReferenceAndEnabledTrueAndTenantCode(
                orderRequestDTO.getSellerReference(),
                tenant
        );
        errorResponse = OrderUtils.validateSeller(orderRequestDTO, seller);
        if (errorResponse != null) return errorResponse;

        Warehouse warehouse = warehouseService.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(
                orderRequestDTO.getWarehouseReference(),
                tenant,
                seller.getId()
        );
        errorResponse = OrderUtils.validateWarehouse(orderRequestDTO, warehouse, seller);
        if (errorResponse != null) return errorResponse;

        PaymentCondition paymentCondition = paymentConditionService.findByCodeAndEnabledTrueAndTenantCode(
                orderRequestDTO.getPaymentConditionCode(),
                tenant
        );
        errorResponse = OrderUtils.validatePaymentCondition(orderRequestDTO, paymentCondition);
        if (errorResponse != null) return errorResponse;

        Order order = new Order();
        order.setExternalReference(orderRequestDTO.getExternalReference());
        order.setOrigin(OrderUtils.getOrderOrigin());
        order.setTenantCode(tenant);
        order.setCreatedAt(OffsetDateTime.now());
        order.setLastModified(order.getCreatedAt());
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setWarehouse(warehouse);
        order.setPaymentCondition(paymentCondition);

        List<OrderItem> orderItems = new ArrayList<>();

        for (ItemDTO requestItemDTO : orderRequestDTO.getItems()) {
            ProductPrice productPrice = productPriceService.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(
                    requestItemDTO.getProductCode(),
                    tenant,
                    warehouse.getId()
            );
            errorResponse = OrderUtils.validateProductPrice(requestItemDTO, productPrice);
            if (errorResponse != null) return errorResponse;

            OrderItem orderItem = new OrderItem();
            orderItem.setProductCode(requestItemDTO.getProductCode());
            orderItem.setProductName(productPrice.getProductName());
            orderItem.setQuantity(requestItemDTO.getQuantity());
            orderItem.setUnitPrice(productPrice.getUnitPrice());
            orderItem.setListPrice(productPrice.getListPrice());
            orderItem.setOrder(order);
            orderItem.calculateSubtotal();
            orderItems.add(orderItem);
            order.setSubtotal(order.getSubtotal().add(orderItem.getSubtotal()));
        }

        errorResponse = OrderUtils.validateBuyersCredit(order, buyer);
        if (errorResponse != null) return errorResponse;

        order.setItems(orderItems);
        order.setTotal(order.getSubtotal());

        OrderResponseDTO.DiscountDTO discountDTO = orderRuleEngine.processOrder(order, tenant, paymentCondition.getCode());

        order.setStatus(OrderStatus.COMPLETED);
        order.setCode(generateCode());

        if (!paymentCondition.getAllowBonus()) {
            ResponseEntity<ErrorResponseDTO> errorResponseDTO = decreaseBuyersLimit(order, buyer);
            if (errorResponseDTO != null) return errorResponseDTO;
        }

        Order orderSaved = save(order);

        UUID correlationId = UUID.randomUUID();

        OrderPayload payload = new OrderPayload();
        payload.setExternalReference(orderSaved.getExternalReference());
        payload.setBuyerReference(orderSaved.getBuyer().getExternalReference());
        payload.setOrderId(orderSaved.getId());
        payload.setTotal(orderSaved.getTotal());
        payload.setStatus(orderSaved.getStatus());

        orderEventProducer.publishOrderCreatedEvent(payload, tenant, correlationId);
        log.info("Evento ORDER_CREATED publicado para o pedido: {}, correlationId: {}", payload.getOrderId(), correlationId);

        OrderResponseDTO orderResponseDTO = OrderUtils.populateOrderResponseDTO(orderSaved, paymentCondition, discountDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDTO);
    }

    public ResponseEntity<ErrorResponseDTO> decreaseBuyersLimit(Order order, Buyer buyer) {
        try {
            log.info("Iniciando decremento atômico de crédito para pedido: {}", order.getCode());
            buyerService.decrementCreditAtomically(buyer.getId(), order.getTotal());
            log.info("Crédito decrementado com sucesso após aprovação do pedido: {}", order.getCode());
        } catch (IllegalStateException ex) {
            log.error("Erro ao decrementar crédito: {}", ex.getMessage());
            order.setStatus(OrderStatus.PENDING);
            throw ex;
        }
        return null;
    }

    public ResponseEntity<ErrorResponseDTO> increaseBuyersLimit(Order order, UUID buyerId) {
        try {
            log.info("Iniciando incremento atômico de crédito para pedido: {}", order.getCode());
            buyerService.increaseCreditAtomically(buyerId, order.getTotal());
            log.info("Crédito incrementado com sucesso após aprovação do pedido: {}", order.getCode());
        } catch (IllegalStateException ex) {
            log.error("Erro ao incrementar crédito: {}", ex.getMessage());
            order.setStatus(OrderStatus.PENDING);
            save(order);

            ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
            errorResponseDTO.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            errorResponseDTO.setMessage("Falha ao processar crédito: " + ex.getMessage());
            errorResponseDTO.setDetails(List.of(
                    "O pedido foi cancelado mas a devolução de crédito falhou",
                    "Mensagem: " + ex.getMessage()
            ));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    @Override
    public Order save(Order order) {
        order.setTenantCode(TenantContext.getTenant());
        if (order.getId() == null) {
            order.setCreatedAt(OffsetDateTime.now());
            order.setLastModified(order.getCreatedAt());
        } else {
            order.setLastModified(OffsetDateTime.now());
        }
        return orderRepository.save(order);
    }

    @Override
    public String generateCode() {
        return String.format("ORDER-%03d", (orderRepository.count() + 1));
    }

    @Override
    public ResponseEntity<?> listPaginated(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            OrderStatus status,
            String buyerRef,
            String tenantCode,
            Integer page,
            Integer size
    ) {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        if (size > 50) {
            ErrorResponseDTO errorResponse = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_008);
            errorResponse.setDetails(
                    List.of(
                            "Escolha um valor para o tamanho da pagina entre 1 e 50. Default é 20."
                    )
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        Page<OrderListingProjection> pageOrderListingProjection = orderRepository.findByStarDateAndEndDateAndTenantCode(
                startDate,
                endDate,
                status,
                buyerRef,
                tenantCode,
                Pageable.ofSize(size).withPage(page)
        );
        return ResponseEntity.ok(new OrderPageResponseDTO<>(pageOrderListingProjection));
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelOrder(String externalReference) {
        String tenant = TenantContext.getTenant();
        ResponseEntity<ErrorResponseDTO> errorResponse = checkIfTenantIsPresent(tenant);
        if (errorResponse != null) return errorResponse;

        Order order = orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenant);

        errorResponse = checkIfOrderIsPresent(order, externalReference);
        if (errorResponse != null) return errorResponse;

        errorResponse = checkIfOrderIsAlreadyCanceled(externalReference, order);
        if (errorResponse != null) return errorResponse;

        Buyer buyer = order.getBuyer();

        if (!order.getPaymentCondition().getAllowBonus()) {
            ResponseEntity<ErrorResponseDTO> errorResponseDTO = increaseBuyersLimit(order, buyer.getId());
            if (errorResponseDTO != null) return errorResponseDTO;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order = save(order);

        UUID correlationId = UUID.randomUUID();

        OrderPayload payload = new OrderPayload();
        payload.setExternalReference(order.getExternalReference());
        payload.setBuyerReference(order.getBuyer().getExternalReference());
        payload.setOrderId(order.getId());
        payload.setTotal(order.getTotal());
        payload.setStatus(order.getStatus());

        orderEventProducer.publishOrderCancelledEvent(payload, tenant, correlationId);
        log.info("Evento ORDER_CANCELLED publicado para o pedido: {}, correlationId: {}", payload.getOrderId(), correlationId);

        return ResponseEntity.ok(convertOrderToOrderDTO(order));
    }

    @Override
    @Transactional
    public ResponseEntity<?> findByExternatReference(String externalReference) {
        String tenant = TenantContext.getTenant();
        ResponseEntity<ErrorResponseDTO> errorResponse = checkIfTenantIsPresent(tenant);
        if (errorResponse != null) return errorResponse;

        Order order = orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenant);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertOrderToOrderDTO(order));
    }

    @Override
    public StatisticsDTO generateReport(String tenant, OffsetDateTime from, OffsetDateTime to) {
        OrderSummaryProjection summary = orderRepository.getSummary(tenant, from, to);
        List<TopBuyerDTO> topBuyers = buyerService.findTopBuyers(tenant, from, to);
        List<TopProductDTO> topProducts = productPriceService.findTopProducts(tenant, from, to);

        return new StatisticsDTO(
                tenant,
                new PeriodDTO(from, to),
                summary.getTotalOrders(),
                summary.getConfirmedOrders(),
                summary.getCancelledOrders(),
                summary.getTotalRevenue(),
                summary.getAverageOrderValue(),
                topBuyers,
                topProducts
        );
    }

    public OrderDTO convertOrderToOrderDTO(Order order) {
        OrderDTO orderDTO = orderMapper.toDto(order);

        List<OrderItemDTO> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            OrderItemDTO itemDTO = orderMapper.toDto(item);
            items.add(itemDTO);
        }

        orderDTO.setItems(items);

        orderDTO.setBuyer(buyerMapper.toDto(order.getBuyer()));
        orderDTO.setSeller(sellerMapper.toDto(order.getSeller()));
        orderDTO.setWarehouse(warehouseMapper.toDto(order.getWarehouse()));
        orderDTO.setPaymentCondition(paymentConditionMapper.toDto(order.getPaymentCondition()));
        return orderDTO;
    }
}
