package br.com.b2list.service;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.domain.dto.ItemDTO;
import br.com.b2list.domain.dto.OrderDTO;
import br.com.b2list.domain.dto.OrderPageResponseDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.dto.StatisticsDTO;
import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.domain.entity.ProductPrice;
import br.com.b2list.domain.entity.Seller;
import br.com.b2list.domain.entity.Warehouse;
import br.com.b2list.enums.Error;
import br.com.b2list.enums.OrderOrigin;
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
import br.com.b2list.service.impl.OrderRuleEngine;
import br.com.b2list.service.impl.OrderServiceImpl;
import br.com.b2list.tenant.TenantContext;
import br.com.b2list.util.ErrorUtil;
import br.com.b2list.util.OrderUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private BuyerService buyerService;
    @Mock
    private SellerService sellerService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private PaymentConditionService paymentConditionService;
    @Mock
    private ProductPriceService productPriceService;
    @Mock
    private OrderRuleEngine orderRuleEngine;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private BuyerMapper buyerMapper;
    @Mock
    private SellerMapper sellerMapper;
    @Mock
    private WarehouseMapper warehouseMapper;
    @Mock
    private PaymentConditionMapper paymentConditionMapper;

    @Spy
    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    private OrderRequestDTO orderRequestDTO;
    private String tenantCode;
    private UUID buyerId;
    private UUID sellerId;
    private UUID warehouseId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        tenantCode = "TEST_TENANT";
        buyerId = UUID.randomUUID();
        sellerId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setExternalReference("EXT-REF-123");
        orderRequestDTO.setBuyerReference("BUYER-REF-001");
        orderRequestDTO.setSellerReference("SELLER-REF-001");
        orderRequestDTO.setWarehouseReference("WAREHOUSE-REF-001");
        orderRequestDTO.setPaymentConditionCode("COND-PAG-001");

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setProductCode("PROD-001");
        itemDTO.setQuantity(2);
        orderRequestDTO.setItems(List.of(itemDTO));
    }

    @Test
    @DisplayName("O pedido deve ser criado com sucesso quando allowBonus for verdadeiro")
    void shouldCreateOrderSuccessfullyWhenAllowBonusIsTrue() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfExternalReferenceIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfBuyerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfSellerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfWarehouseIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfPaymentConditionIsPresent(any(OrderRequestDTO.class))).thenReturn(null);

            Buyer mockBuyer = new Buyer();
            mockBuyer.setId(buyerId);
            mockBuyer.setExternalReference("BUYER-REF-001");
            when(buyerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockBuyer);
            mockedOrderUtils.when(() -> OrderUtils.validateBuyer(any(OrderRequestDTO.class), any(Buyer.class))).thenReturn(null);

            Seller mockSeller = new Seller();
            mockSeller.setId(sellerId);
            mockSeller.setExternalReference("SELLER-REF-001");
            when(sellerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockSeller);
            mockedOrderUtils.when(() -> OrderUtils.validateSeller(any(OrderRequestDTO.class), any(Seller.class))).thenReturn(null);

            Warehouse mockWarehouse = new Warehouse();
            mockWarehouse.setId(warehouseId);
            mockWarehouse.setExternalReference("WAREHOUSE-REF-001");
            when(warehouseService.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(anyString(), anyString(), any(UUID.class))).thenReturn(mockWarehouse);
            mockedOrderUtils.when(() -> OrderUtils.validateWarehouse(any(OrderRequestDTO.class), any(Warehouse.class), any(Seller.class))).thenReturn(null);

            PaymentCondition mockPaymentCondition = new PaymentCondition();
            mockPaymentCondition.setCode("COND-PAG-001");
            mockPaymentCondition.setAllowBonus(true);
            when(paymentConditionService.findByCodeAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockPaymentCondition);
            mockedOrderUtils.when(() -> OrderUtils.validatePaymentCondition(any(OrderRequestDTO.class), any(PaymentCondition.class))).thenReturn(null);

            ProductPrice mockProductPrice = new ProductPrice();
            mockProductPrice.setProductCode("PROD-001");
            mockProductPrice.setProductName("Produto Teste");
            mockProductPrice.setUnitPrice(BigDecimal.valueOf(100.00));
            mockProductPrice.setListPrice(BigDecimal.valueOf(120.00));
            when(productPriceService.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(anyString(), anyString(), any(UUID.class))).thenReturn(mockProductPrice);
            mockedOrderUtils.when(() -> OrderUtils.validateProductPrice(any(ItemDTO.class), any(ProductPrice.class))).thenReturn(null);

            mockedOrderUtils.when(OrderUtils::getOrderOrigin).thenReturn(OrderOrigin.API);

            OrderResponseDTO.DiscountDTO mockDiscountDTO = new OrderResponseDTO.DiscountDTO();
            mockDiscountDTO.setValue(BigDecimal.valueOf(20.00));
            when(orderRuleEngine.processOrder(any(Order.class), any(), any())).thenReturn(mockDiscountDTO);

            doReturn("ORDER-001").when(orderServiceImpl).generateCode();

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setExternalReference(orderRequestDTO.getExternalReference());
            savedOrder.setTenantCode(tenantCode);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder.setBuyer(mockBuyer);
            savedOrder.setTotal(BigDecimal.valueOf(200.00));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyersCredit(any(Order.class), any(Buyer.class))).thenReturn(null);

            OrderResponseDTO mockOrderResponseDTO = new OrderResponseDTO();
            mockOrderResponseDTO.setCode(savedOrder.getCode());
            mockedOrderUtils.when(() -> OrderUtils.populateOrderResponseDTO(any(Order.class), any(PaymentCondition.class), any(OrderResponseDTO.DiscountDTO.class)))
                    .thenReturn(mockOrderResponseDTO);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertInstanceOf(OrderResponseDTO.class, response.getBody());

            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer).publishOrderCreatedEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
            verify(orderServiceImpl, never()).decreaseBuyersLimit(any(Order.class), any(Buyer.class));
        }
    }

    @Test
    @DisplayName("Deve-se chamar decreaseBuyersLimit quando allowBonus for falso.")
    void shouldCallDecreaseBuyersLimitWhenAllowBonusIsFalse() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfExternalReferenceIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfBuyerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfSellerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfWarehouseIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfPaymentConditionIsPresent(any(OrderRequestDTO.class))).thenReturn(null);

            Buyer mockBuyer = new Buyer();
            mockBuyer.setId(buyerId);
            mockBuyer.setExternalReference("BUYER-REF-001");
            when(buyerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockBuyer);
            mockedOrderUtils.when(() -> OrderUtils.validateBuyer(any(OrderRequestDTO.class), any(Buyer.class))).thenReturn(null);

            Seller mockSeller = new Seller();
            mockSeller.setId(sellerId);
            when(sellerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockSeller);
            mockedOrderUtils.when(() -> OrderUtils.validateSeller(any(OrderRequestDTO.class), any(Seller.class))).thenReturn(null);

            Warehouse mockWarehouse = new Warehouse();
            mockWarehouse.setId(warehouseId);
            when(warehouseService.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(anyString(), anyString(), any(UUID.class))).thenReturn(mockWarehouse);
            mockedOrderUtils.when(() -> OrderUtils.validateWarehouse(any(OrderRequestDTO.class), any(Warehouse.class), any(Seller.class))).thenReturn(null);

            PaymentCondition mockPaymentCondition = new PaymentCondition();
            mockPaymentCondition.setCode("COND-PAG-001");
            mockPaymentCondition.setAllowBonus(false);
            when(paymentConditionService.findByCodeAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockPaymentCondition);
            mockedOrderUtils.when(() -> OrderUtils.validatePaymentCondition(any(OrderRequestDTO.class), any(PaymentCondition.class))).thenReturn(null);

            ProductPrice mockProductPrice = new ProductPrice();
            mockProductPrice.setUnitPrice(BigDecimal.valueOf(100.00));
            when(productPriceService.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(anyString(), anyString(), any(UUID.class))).thenReturn(mockProductPrice);
            mockedOrderUtils.when(() -> OrderUtils.validateProductPrice(any(ItemDTO.class), any(ProductPrice.class))).thenReturn(null);

            mockedOrderUtils.when(OrderUtils::getOrderOrigin).thenReturn(OrderOrigin.API);

            OrderResponseDTO.DiscountDTO mockDiscountDTO = new OrderResponseDTO.DiscountDTO();
            when(orderRuleEngine.processOrder(any(Order.class), any(), any())).thenReturn(mockDiscountDTO);

            doReturn("ORDER-CODE-123").when(orderServiceImpl).generateCode();
            doReturn(null).when(orderServiceImpl).decreaseBuyersLimit(any(Order.class), any(Buyer.class));

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setTenantCode(tenantCode);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder.setBuyer(mockBuyer);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyersCredit(any(Order.class), any(Buyer.class))).thenReturn(null);

            OrderResponseDTO mockOrderResponseDTO = new OrderResponseDTO();
            mockedOrderUtils.when(() -> OrderUtils.populateOrderResponseDTO(any(Order.class), any(PaymentCondition.class), any(OrderResponseDTO.DiscountDTO.class)))
                    .thenReturn(mockOrderResponseDTO);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            verify(orderServiceImpl).decreaseBuyersLimit(any(Order.class), eq(mockBuyer));
            verify(orderEventProducer).publishOrderCreatedEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar um erro quando decreaseBuyersLimit falhar.")
    void shouldReturnErrorWhenDecreaseBuyersLimitFails() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfExternalReferenceIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfBuyerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfSellerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfWarehouseIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfPaymentConditionIsPresent(any(OrderRequestDTO.class))).thenReturn(null);

            Buyer mockBuyer = new Buyer();
            mockBuyer.setId(buyerId);
            when(buyerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockBuyer);
            mockedOrderUtils.when(() -> OrderUtils.validateBuyer(any(OrderRequestDTO.class), any(Buyer.class))).thenReturn(null);

            Seller mockSeller = new Seller();
            mockSeller.setId(sellerId);
            when(sellerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockSeller);
            mockedOrderUtils.when(() -> OrderUtils.validateSeller(any(OrderRequestDTO.class), any(Seller.class))).thenReturn(null);

            Warehouse mockWarehouse = new Warehouse();
            mockWarehouse.setId(warehouseId);
            when(warehouseService.findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(anyString(), anyString(), any(UUID.class))).thenReturn(mockWarehouse);
            mockedOrderUtils.when(() -> OrderUtils.validateWarehouse(any(OrderRequestDTO.class), any(Warehouse.class), any(Seller.class))).thenReturn(null);

            PaymentCondition mockPaymentCondition = new PaymentCondition();
            mockPaymentCondition.setCode("COND-PAG-001");
            mockPaymentCondition.setAllowBonus(false);
            when(paymentConditionService.findByCodeAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockPaymentCondition);
            mockedOrderUtils.when(() -> OrderUtils.validatePaymentCondition(any(OrderRequestDTO.class), any(PaymentCondition.class))).thenReturn(null);

            ProductPrice mockProductPrice = new ProductPrice();
            mockProductPrice.setProductCode("PROD-001");
            mockProductPrice.setProductName("Produto Teste");
            mockProductPrice.setUnitPrice(BigDecimal.valueOf(100.00));
            mockProductPrice.setListPrice(BigDecimal.valueOf(120.00));
            when(productPriceService.findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(anyString(), anyString(), any(UUID.class))).thenReturn(mockProductPrice);
            mockedOrderUtils.when(() -> OrderUtils.validateProductPrice(any(ItemDTO.class), any(ProductPrice.class))).thenReturn(null);

            mockedOrderUtils.when(OrderUtils::getOrderOrigin).thenReturn(OrderOrigin.API);

            OrderResponseDTO.DiscountDTO mockDiscountDTO = new OrderResponseDTO.DiscountDTO();
            when(orderRuleEngine.processOrder(any(Order.class), any(), any())).thenReturn(mockDiscountDTO);

            doReturn("ORDER-CODE-123").when(orderServiceImpl).generateCode();
            ErrorResponseDTO decreaseLimitError = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_003);
            doReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(decreaseLimitError))
                    .when(orderServiceImpl).decreaseBuyersLimit(any(Order.class), any(Buyer.class));

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setExternalReference(orderRequestDTO.getExternalReference());
            savedOrder.setTenantCode(tenantCode);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder.setTotal(BigDecimal.valueOf(200.00));
            savedOrder.setSubtotal(BigDecimal.valueOf(200.00));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyersCredit(any(Order.class), any(Buyer.class))).thenReturn(null);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertInstanceOf(ErrorResponseDTO.class, response.getBody());

            verify(orderEventProducer, never()).publishOrderCreatedEvent(any(OrderPayload.class), anyString(), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar erro quando o tenant não estiver presente.\n")
    void shouldReturnErrorWhenTenantIsNotPresent() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            ErrorResponseDTO errorResponse = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_005);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString()))
                    .thenReturn(ResponseEntity.badRequest().body(errorResponse));

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponseDTO.class, response.getBody());

            verifyNoInteractions(buyerService, sellerService, warehouseService, paymentConditionService, productPriceService, orderRepository);
        }
    }

    @Test
    @DisplayName("Deve diminuir com sucesso o limite do comprador")
    void shouldSuccessfullyDecreaseBuyersLimit() {
        Order order = new Order();
        order.setCode("ORDER-001");
        order.setTotal(BigDecimal.valueOf(100.00));
        Buyer buyer = new Buyer();
        buyer.setId(buyerId);

        ResponseEntity<ErrorResponseDTO> response = orderServiceImpl.decreaseBuyersLimit(order, buyer);

        assertNull(response);
        verify(buyerService).decrementCreditAtomically(eq(buyerId), eq(BigDecimal.valueOf(100.00)));
    }

    @Test
    @DisplayName("Deve retornar um erro quando a redução do limite do comprador falhar.\n")
    void shouldReturnErrorWhenDecreasingBuyersLimitFails() {
        Order order = new Order();
        order.setCode("ORDER-002");
        order.setTotal(BigDecimal.valueOf(250.00));
        order.setStatus(OrderStatus.COMPLETED);
        String errorMessage = "Crédito insuficiente para decremento.";

        doThrow(new IllegalStateException(errorMessage))
                .when(buyerService).decrementCreditAtomically(any(UUID.class), any(BigDecimal.class));

        doAnswer(invocation -> {
            Order argOrder = invocation.getArgument(0);
            assertEquals(OrderStatus.PENDING, argOrder.getStatus());
            return argOrder;
        }).when(orderServiceImpl).save(any(Order.class));

        Buyer buyer = new Buyer();
        buyer.setId(buyerId);

        ResponseEntity<ErrorResponseDTO> response = orderServiceImpl.decreaseBuyersLimit(order, buyer);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Falha ao processar crédito: " + errorMessage, response.getBody().getMessage());
        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(buyerService).decrementCreditAtomically(eq(buyerId), eq(BigDecimal.valueOf(250.00)));
    }

    @Test
    @DisplayName("Deverá aumentar com sucesso o limite do comprador")
    void shouldSuccessfullyIncreaseBuyersLimit() {
        Order order = new Order();
        order.setCode("ORDER-001");
        order.setTotal(BigDecimal.valueOf(100.00));
        UUID buyerUUID = UUID.randomUUID();

        ResponseEntity<ErrorResponseDTO> response = orderServiceImpl.increaseBuyersLimit(order, buyerUUID);

        assertNull(response);
        verify(buyerService).increaseCreditAtomically(eq(buyerUUID), eq(order.getTotal()));
    }

    @Test
    @DisplayName("Deve retornar um erro quando o aumento do limite do comprador falhar")
    void shouldReturnErrorWhenIncreasingBuyersLimitFails() {
        Order order = new Order();
        order.setCode("ORDER-002");
        order.setTotal(BigDecimal.valueOf(250.00));
        order.setStatus(OrderStatus.COMPLETED);
        UUID buyerUUID = UUID.randomUUID();
        String errorMessage = "Crédito insuficiente para incremento.";

        doThrow(new IllegalStateException(errorMessage))
                .when(buyerService).increaseCreditAtomically(any(UUID.class), any(BigDecimal.class));

        doAnswer(invocation -> {
            Order argOrder = invocation.getArgument(0);
            assertEquals(OrderStatus.PENDING, argOrder.getStatus());
            return argOrder;
        }).when(orderServiceImpl).save(any(Order.class));

        ResponseEntity<ErrorResponseDTO> response = orderServiceImpl.increaseBuyersLimit(order, buyerUUID);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Falha ao processar crédito: " + errorMessage, response.getBody().getMessage());
        assertThat(response.getBody().getDetails())
                .isNotNull()
                .isNotEmpty()
                .contains("O pedido foi cancelado mas a devolução de crédito falhou");
        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(buyerService).increaseCreditAtomically(eq(buyerUUID), eq(BigDecimal.valueOf(250.00)));
        verify(orderServiceImpl).save(eq(order));
    }

    @Test
    @DisplayName("O cancelamento do pedido deve ser bem-sucedido quando allowBonus for verdadeiro")
    void shouldCancelOrderSuccessfullyWhenAllowBonusIsTrue() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            String externalReference = "EXT-REF-123";
            PaymentCondition bonusPaymentCondition = new PaymentCondition();
            bonusPaymentCondition.setAllowBonus(true);
            Order completedOrder = createOrder(externalReference, OrderStatus.COMPLETED, bonusPaymentCondition);

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(completedOrder);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsPresent(any(Order.class), anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsAlreadyCanceled(anyString(), any(Order.class))).thenReturn(null);

            Order cancelledOrder = createOrder(externalReference, OrderStatus.CANCELLED, bonusPaymentCondition);
            when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

            OrderDTO mockOrderDTO = new OrderDTO();
            when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDTO);
            when(buyerMapper.toDto(any(Buyer.class))).thenReturn(new br.com.b2list.domain.dto.BuyerDTO());
            when(sellerMapper.toDto(any(Seller.class))).thenReturn(new br.com.b2list.domain.dto.SellerDTO());
            when(warehouseMapper.toDto(any(Warehouse.class))).thenReturn(new br.com.b2list.domain.dto.WarehouseDTO());
            when(paymentConditionMapper.toDto(any(PaymentCondition.class))).thenReturn(new br.com.b2list.domain.dto.PaymentConditionDTO());

            ResponseEntity<?> response = orderServiceImpl.cancelOrder(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer).publishOrderCancelledEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
            verify(orderServiceImpl, never()).increaseBuyersLimit(any(Order.class), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve cancelar o pedido e aumentar o limite do comprador quando allowBonus for falso.\n")
    void shouldCancelOrderAndIncreaseBuyersLimitWhenAllowBonusIsFalse() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            String externalReference = "EXT-REF-123";
            PaymentCondition nonBonusPaymentCondition = new PaymentCondition();
            nonBonusPaymentCondition.setAllowBonus(false);
            Order completedOrder = createOrder(externalReference, OrderStatus.COMPLETED, nonBonusPaymentCondition);

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(completedOrder);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsPresent(any(Order.class), anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsAlreadyCanceled(anyString(), any(Order.class))).thenReturn(null);

            doReturn(null).when(orderServiceImpl).increaseBuyersLimit(any(Order.class), any(UUID.class));

            Order cancelledOrder = createOrder(externalReference, OrderStatus.CANCELLED, nonBonusPaymentCondition);
            when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

            OrderDTO mockOrderDTO = new OrderDTO();
            when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDTO);
            when(buyerMapper.toDto(any(Buyer.class))).thenReturn(new br.com.b2list.domain.dto.BuyerDTO());
            when(sellerMapper.toDto(any(Seller.class))).thenReturn(new br.com.b2list.domain.dto.SellerDTO());
            when(warehouseMapper.toDto(any(Warehouse.class))).thenReturn(new br.com.b2list.domain.dto.WarehouseDTO());
            when(paymentConditionMapper.toDto(any(PaymentCondition.class))).thenReturn(new br.com.b2list.domain.dto.PaymentConditionDTO());

            ResponseEntity<?> response = orderServiceImpl.cancelOrder(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());

            verify(orderServiceImpl).increaseBuyersLimit(any(Order.class), eq(buyerId));
            verify(orderEventProducer).publishOrderCancelledEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar um erro quando o pedido não for encontrado durante o cancelamento")
    void shouldReturnErrorWhenOrderNotFoundDuringCancellation() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            String externalReference = "EXT-REF-NONEXISTENT";
            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(null);

            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsPresent(null, externalReference))
                    .thenReturn(ResponseEntity.notFound().build());

            ResponseEntity<?> response = orderServiceImpl.cancelOrder(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

            verify(orderEventProducer, never()).publishOrderCancelledEvent(any(OrderPayload.class), anyString(), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar um erro quando o aumento do limite do comprador falhar durante o cancelamento")
    void shouldReturnErrorWhenIncreasingBuyersLimitFailsDuringCancellation() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            String externalReference = "EXT-REF-123";
            String errorMessage = "Insufficient credit to increase";

            PaymentCondition nonBonusPaymentCondition = new PaymentCondition();
            nonBonusPaymentCondition.setAllowBonus(false);
            Order completedOrder = createOrder(externalReference, OrderStatus.COMPLETED, nonBonusPaymentCondition);

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(completedOrder);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsPresent(any(Order.class), anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsAlreadyCanceled(anyString(), any(Order.class))).thenReturn(null);


            ErrorResponseDTO expectedError = new ErrorResponseDTO();
            expectedError.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            expectedError.setMessage("Falha ao processar crédito: " + errorMessage);
            doReturn(ResponseEntity.unprocessableEntity().body(expectedError))
                    .when(orderServiceImpl).increaseBuyersLimit(any(Order.class), any(UUID.class));

            ResponseEntity<?> response = orderServiceImpl.cancelOrder(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

            verify(orderEventProducer, never()).publishOrderCancelledEvent(any(OrderPayload.class), anyString(), any(UUID.class));
        }
    }

    @Test
    @DisplayName("A lista de pedidos deve ser paginada com sucesso")
    void shouldListOrdersPaginatedSuccessfully() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            List<OrderListingProjection> orderList = new ArrayList<>();
            Page<OrderListingProjection> pageResult = new PageImpl<>(orderList);
            when(orderRepository.findByStarDateAndEndDateAndTenantCode(any(), any(), any(), anyString(), anyString(), any(Pageable.class)))
                    .thenReturn(pageResult);

            OffsetDateTime startDate = OffsetDateTime.now().minusDays(10);
            OffsetDateTime endDate = OffsetDateTime.now();

            ResponseEntity<?> response = orderServiceImpl.listPaginated(startDate, endDate, OrderStatus.COMPLETED, "BUYER-001", tenantCode, 0, 20);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(OrderPageResponseDTO.class, response.getBody());
        }
    }

    @Test
    @DisplayName("Deve retornar um erro quando o tamanho da página exceder o máximo.\n")
    void shouldReturnErrorWhenPageSizeExceedsMaximum() {
        ResponseEntity<?> response = orderServiceImpl.listPaginated(OffsetDateTime.now(), OffsetDateTime.now(), null, null, tenantCode, 0, 100);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(ErrorResponseDTO.class, response.getBody());
    }

    @Test
    @DisplayName("Deve encontrar o pedido por referência externa com sucesso.")
    void shouldFindOrderByExternalReferenceSuccessfully() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            String externalReference = "EXT-REF-123";
            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            Order order = createOrder(externalReference, OrderStatus.COMPLETED, new PaymentCondition());
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(order);

            OrderDTO mockOrderDTO = new OrderDTO();
            when(orderMapper.toDto(any(Order.class))).thenReturn(mockOrderDTO);
            when(buyerMapper.toDto(any(Buyer.class))).thenReturn(new br.com.b2list.domain.dto.BuyerDTO());
            when(sellerMapper.toDto(any(Seller.class))).thenReturn(new br.com.b2list.domain.dto.SellerDTO());
            when(warehouseMapper.toDto(any(Warehouse.class))).thenReturn(new br.com.b2list.domain.dto.WarehouseDTO());
            when(paymentConditionMapper.toDto(any(PaymentCondition.class))).thenReturn(new br.com.b2list.domain.dto.PaymentConditionDTO());

            ResponseEntity<?> response = orderServiceImpl.findByExternatReference(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertInstanceOf(OrderDTO.class, response.getBody());
        }
    }

    @Test
    @DisplayName("Deve retornar 'não encontrado' quando o pedido não existir")
    void shouldReturnNotFoundWhenOrderDoesNotExist() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            String externalReference = "EXT-REF-NONEXISTENT";
            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(null);

            ResponseEntity<?> response = orderServiceImpl.findByExternatReference(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("Deve salvar o pedido com sucesso\n")
    void shouldSaveOrderSuccessfully() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);

            Order order = new Order();
            order.setStatus(OrderStatus.COMPLETED);

            when(orderRepository.save(any(Order.class))).thenReturn(order);

            Order savedOrder = orderServiceImpl.save(order);

            assertNotNull(savedOrder);
            assertEquals(OrderStatus.COMPLETED, savedOrder.getStatus());
            verify(orderRepository).save(order);
        }
    }

    @Test
    @DisplayName("Deve gerar código de pedido")
    void shouldGenerateOrderCode() {
        when(orderRepository.count()).thenReturn(5L);

        String code = orderServiceImpl.generateCode();

        assertNotNull(code);
        assertEquals("ORDER-006", code);
    }

    @Test
    @DisplayName("Deve gerar relatório com sucesso\n")
    void shouldGenerateReportSuccessfully() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(30);
        OffsetDateTime to = OffsetDateTime.now();

        OrderSummaryProjection mockSummary = new OrderSummaryProjection() {
            @Override
            public Long getTotalOrders() { return 100L; }
            @Override
            public Long getConfirmedOrders() { return 95L; }
            @Override
            public Long getCancelledOrders() { return 5L; }
            @Override
            public BigDecimal getTotalRevenue() { return BigDecimal.valueOf(10000.00); }
            @Override
            public BigDecimal getAverageOrderValue() { return BigDecimal.valueOf(100.00); }
        };

        when(orderRepository.getSummary(tenantCode, from, to)).thenReturn(mockSummary);
        when(buyerService.findTopBuyers(tenantCode, from, to)).thenReturn(new ArrayList<>());
        when(productPriceService.findTopProducts(tenantCode, from, to)).thenReturn(new ArrayList<>());

        StatisticsDTO report = orderServiceImpl.generateReport(tenantCode, from, to);

        assertNotNull(report);
        assertEquals(tenantCode, report.getTenant());
        assertEquals(100L, report.getTotalOrders());
        assertEquals(95L, report.getConfirmedOrders());
        assertEquals(5L, report.getCancelledOrders());

        verify(orderRepository).getSummary(tenantCode, from, to);
    }

    private Order createOrder(String externalReference, OrderStatus status, PaymentCondition paymentCondition) {
        Order order = new Order();
        order.setId(orderId);
        order.setExternalReference(externalReference);
        order.setTenantCode(tenantCode);
        order.setStatus(status);
        order.setCode("ORDER-001");
        order.setTotal(BigDecimal.valueOf(100.00));
        order.setPaymentCondition(paymentCondition);

        Buyer buyer = new Buyer();
        buyer.setId(buyerId);
        buyer.setExternalReference("BUYER-REF-001");
        order.setBuyer(buyer);

        Seller seller = new Seller();
        seller.setId(sellerId);
        order.setSeller(seller);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        order.setWarehouse(warehouse);

        return order;
    }
}

