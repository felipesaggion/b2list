package br.com.b2list.service;

import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.domain.dto.ItemDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.dto.OrderResult;
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
import br.com.b2list.producer.OrderEventProducer;
import br.com.b2list.repository.OrderRepository;
import br.com.b2list.service.impl.GenericPaymentCalculator;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    private GenericPaymentCalculator genericPaymentCalculator;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private OrderRepository orderRepository;

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
    @DisplayName("Deve criar um pedido com sucesso e publicar evento")
    void shouldCreateOrderSuccessfullyAndPublishEvent() {
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

            OrderResult mockOrderResult = new OrderResult();
            mockOrderResult.setTotal(BigDecimal.valueOf(200.00));
            mockOrderResult.setAllowBonusOrder(true);
            when(genericPaymentCalculator.process(any(Order.class), any(PaymentCondition.class))).thenReturn(mockOrderResult);

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
            mockedOrderUtils.when(() -> OrderUtils.populateOrderResponseDTO(any(Order.class), any(PaymentCondition.class), any(OrderResult.class)))
                    .thenReturn(mockOrderResponseDTO);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertInstanceOf(OrderResponseDTO.class, response.getBody());
            assertEquals(savedOrder.getCode(), ((OrderResponseDTO) response.getBody()).getCode());

            verify(buyerService).findByExternalReferenceAndEnabledTrueAndTenantCode(orderRequestDTO.getBuyerReference(), tenantCode);
            verify(sellerService).findByExternalReferenceAndEnabledTrueAndTenantCode(orderRequestDTO.getSellerReference(), tenantCode);
            verify(warehouseService).findByExternalReferenceAndEnabledTrueAndTenantCodeAndSellerId(orderRequestDTO.getWarehouseReference(), tenantCode, sellerId);
            verify(paymentConditionService).findByCodeAndEnabledTrueAndTenantCode(orderRequestDTO.getPaymentConditionCode(), tenantCode);
            verify(productPriceService).findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(orderRequestDTO.getItems().getFirst().getProductCode(), tenantCode, warehouseId);
            verify(genericPaymentCalculator).process(any(Order.class), eq(mockPaymentCondition));
            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer).publishOrderCreatedEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
            verify(orderServiceImpl, never()).decreaseBuyersLimit(any(Order.class), any(Buyer.class));
        }
    }

    @Test
    @DisplayName("Deve retornar erro se o tenant não estiver presente")
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
            assertEquals("ORD-VALIDATION-005", ((ErrorResponseDTO) response.getBody()).getCode());

            verifyNoInteractions(buyerService, sellerService, warehouseService, paymentConditionService, productPriceService, genericPaymentCalculator, orderEventProducer, orderRepository);
        }
    }

    @Test
    @DisplayName("Deve retornar erro se a validação do comprador falhar")
    void shouldReturnErrorWhenBuyerValidationFails() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfExternalReferenceIsPresent(any(OrderRequestDTO.class))).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfBuyerIsPresent(any(OrderRequestDTO.class))).thenReturn(null);

            Buyer mockBuyer = new Buyer();
            mockBuyer.setId(buyerId);
            mockBuyer.setExternalReference("BUYER-REF-001");
            when(buyerService.findByExternalReferenceAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockBuyer);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyer(eq(orderRequestDTO), any(Buyer.class)))
                    .thenReturn(ResponseEntity.badRequest().body(ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_002)));

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertInstanceOf(ErrorResponseDTO.class, response.getBody());
            assertEquals("ORD-VALIDATION-002", ((ErrorResponseDTO) response.getBody()).getCode());

            verify(buyerService).findByExternalReferenceAndEnabledTrueAndTenantCode(orderRequestDTO.getBuyerReference(), tenantCode);
            verifyNoMoreInteractions(sellerService, warehouseService, paymentConditionService, productPriceService, genericPaymentCalculator, orderEventProducer, orderRepository);
        }
    }

    @Test
    @DisplayName("Deve chamar decreaseBuyersLimit quando allowBonusOrder for falso")
    void shouldCallDecreaseBuyersLimitWhenBonusOrderIsFalse() {
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

            OrderResult mockOrderResult = new OrderResult();
            mockOrderResult.setTotal(BigDecimal.valueOf(200.00));
            mockOrderResult.setAllowBonusOrder(false);
            when(genericPaymentCalculator.process(any(Order.class), any(PaymentCondition.class))).thenReturn(mockOrderResult);

            doReturn("ORDER-CODE-123").when(orderServiceImpl).generateCode();
            doReturn(null).when(orderServiceImpl).decreaseBuyersLimit(any(Order.class), any(Buyer.class));

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
            mockedOrderUtils.when(() -> OrderUtils.populateOrderResponseDTO(any(Order.class), any(PaymentCondition.class), any(OrderResult.class)))
                    .thenReturn(mockOrderResponseDTO);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            verify(orderServiceImpl).decreaseBuyersLimit(any(Order.class), eq(mockBuyer));
            verify(orderEventProducer).publishOrderCreatedEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar erro se decreaseBuyersLimit falhar")
    void shouldReturnErrorIfDecreaseBuyersLimitFails() {
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

            OrderResult mockOrderResult = new OrderResult();
            mockOrderResult.setTotal(BigDecimal.valueOf(200.00));
            mockOrderResult.setAllowBonusOrder(false);
            when(genericPaymentCalculator.process(any(Order.class), any(PaymentCondition.class))).thenReturn(mockOrderResult);

            doReturn("ORDER-CODE-123").when(orderServiceImpl).generateCode();
            ErrorResponseDTO decreaseLimitError = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_003);
            doReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(decreaseLimitError))
                    .when(orderServiceImpl).decreaseBuyersLimit(any(Order.class), any(Buyer.class));

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setExternalReference(orderRequestDTO.getExternalReference());
            savedOrder.setTenantCode(tenantCode);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder.setBuyer(mockBuyer);
            savedOrder.setTotal(BigDecimal.valueOf(200.00));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyersCredit(any(Order.class), any(Buyer.class))).thenReturn(null);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertInstanceOf(ErrorResponseDTO.class, response.getBody());
            assertEquals("ORD-VALIDATION-003", ((ErrorResponseDTO) response.getBody()).getCode());

            verify(orderServiceImpl).decreaseBuyersLimit(any(Order.class), eq(mockBuyer));
            verify(orderEventProducer, never()).publishOrderCreatedEvent(any(OrderPayload.class), anyString(), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve criar pedido com lista de itens vazia")
    void shouldCreateOrderWithEmptyItemsList() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            orderRequestDTO.setItems(Collections.emptyList());
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
            when(paymentConditionService.findByCodeAndEnabledTrueAndTenantCode(anyString(), anyString())).thenReturn(mockPaymentCondition);
            mockedOrderUtils.when(() -> OrderUtils.validatePaymentCondition(any(OrderRequestDTO.class), any(PaymentCondition.class))).thenReturn(null);

            mockedOrderUtils.when(OrderUtils::getOrderOrigin).thenReturn(OrderOrigin.API);

            OrderResult mockOrderResult = new OrderResult();
            mockOrderResult.setTotal(BigDecimal.ZERO);
            mockOrderResult.setAllowBonusOrder(true);
            when(genericPaymentCalculator.process(any(Order.class), any(PaymentCondition.class))).thenReturn(mockOrderResult);

            doReturn("ORDER-CODE-123").when(orderServiceImpl).generateCode(); // Mock private generateCode()

            Order savedOrder = new Order();
            savedOrder.setId(orderId);
            savedOrder.setExternalReference(orderRequestDTO.getExternalReference());
            savedOrder.setTenantCode(tenantCode);
            savedOrder.setStatus(OrderStatus.COMPLETED);
            savedOrder.setBuyer(mockBuyer);
            savedOrder.setTotal(BigDecimal.ZERO);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            mockedOrderUtils.when(() -> OrderUtils.validateBuyersCredit(any(Order.class), any(Buyer.class))).thenReturn(null);

            OrderResponseDTO mockOrderResponseDTO = new OrderResponseDTO();
            mockOrderResponseDTO.setCode(savedOrder.getCode());
            mockedOrderUtils.when(() -> OrderUtils.populateOrderResponseDTO(any(Order.class), any(PaymentCondition.class), any(OrderResult.class)))
                    .thenReturn(mockOrderResponseDTO);

            ResponseEntity<?> response = orderServiceImpl.create(orderRequestDTO);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertInstanceOf(OrderResponseDTO.class, response.getBody());
            assertEquals(savedOrder.getCode(), ((OrderResponseDTO) response.getBody()).getCode());

            verify(productPriceService, never()).findByProductCodeAndTenantCodeAndWarehouseIdAndEnabledTrue(anyString(), anyString(), any(UUID.class));
            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer).publishOrderCreatedEvent(any(OrderPayload.class), eq(tenantCode), any(UUID.class));
        }
    }

    @Test
    @DisplayName("Deve retornar erro UNPROCESSABLE_ENTITY quando incremento de crédito falhar durante cancelamento")
    void shouldReturnErrorWhenIncreasingBuyersLimitFails() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class);
             MockedStatic<OrderUtils> mockedOrderUtils = mockStatic(OrderUtils.class)) {

            // Arrange
            String externalReference = "EXT-REF-123";
            BigDecimal orderTotal = BigDecimal.valueOf(100.00);
            String errorMessage = "Insufficient credit to increase";

            PaymentCondition nonBonusPaymentCondition = createPaymentCondition("COND-PAG-001", false);
            Order completedOrder = createOrder(externalReference, OrderStatus.COMPLETED, orderTotal, nonBonusPaymentCondition);

            mockedTenantContext.when(TenantContext::getTenant).thenReturn(tenantCode);
            mockedOrderUtils.when(() -> OrderUtils.checkIfTenantIsPresent(anyString())).thenReturn(null);
            when(orderRepository.findByExternalReferenceAndTenantCode(externalReference, tenantCode)).thenReturn(completedOrder);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsPresent(any(Order.class), anyString())).thenReturn(null);
            mockedOrderUtils.when(() -> OrderUtils.checkIfOrderIsAlreadyCanceled(anyString(), any(Order.class))).thenReturn(null);

            doThrow(new IllegalStateException(errorMessage))
                    .when(buyerService).increaseCreditAtomically(buyerId, orderTotal);

            Order pendingOrder = createOrder(externalReference, OrderStatus.PENDING, orderTotal, nonBonusPaymentCondition);
            when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

            ResponseEntity<?> response = orderServiceImpl.cancelOrder(externalReference);

            assertNotNull(response);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertInstanceOf(ErrorResponseDTO.class, response.getBody());

            ErrorResponseDTO errorResponseDTO = (ErrorResponseDTO) response.getBody();
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), errorResponseDTO.getStatus());
            assertThat(errorResponseDTO.getDetails())
                    .isNotNull()
                    .isNotEmpty()
                    .contains("O pedido foi cancelado mas a devolução de crédito falhou");

            verify(buyerService).increaseCreditAtomically(buyerId, orderTotal);
            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer, never()).publishOrderCancelledEvent(any(OrderPayload.class), anyString(), any(UUID.class));
        }
    }

    @Test
    @DisplayName("increaseBuyersLimit deve retornar erro e atualizar status do pedido em caso de falha")
    void increaseBuyersLimit_shouldReturnErrorAndChangeOrderStatus_onFailure() {
        Order order = new Order();
        order.setCode("ORDER-002");
        order.setTotal(BigDecimal.valueOf(250.00));
        order.setStatus(OrderStatus.COMPLETED); // Status inicial que será alterado
        UUID buyerUUID = UUID.randomUUID();
        String errorMessage = "Crédito insuficiente para devolução.";

        doThrow(new IllegalStateException(errorMessage))
                .when(buyerService).increaseCreditAtomically(any(UUID.class), any(BigDecimal.class));

        doAnswer(invocation -> {
            Order argOrder = invocation.getArgument(0);
            assertEquals(OrderStatus.PENDING, argOrder.getStatus()); // Verifica que o status foi alterado
            return argOrder;
        }).when(orderServiceImpl).save(any(Order.class));


        ResponseEntity<ErrorResponseDTO> response = orderServiceImpl.increaseBuyersLimit(order, buyerUUID);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Falha ao processar crédito: " + errorMessage, response.getBody().getMessage());
        assertEquals("O pedido foi cancelado mas a devolução de crédito falhou", response.getBody().getDetails().get(0));
        assertEquals("Mensagem: " + errorMessage, response.getBody().getDetails().get(1));

        assertEquals(OrderStatus.PENDING, order.getStatus());

        verify(buyerService).increaseCreditAtomically(eq(buyerUUID), eq(BigDecimal.valueOf(250.00)));

        verify(orderServiceImpl).save(eq(order));
    }

    private PaymentCondition createPaymentCondition(String code, boolean allowBonusOrder) {
        PaymentCondition paymentCondition = new PaymentCondition();
        paymentCondition.setCode(code);
        paymentCondition.setAllowBonusOrder(allowBonusOrder);
        return paymentCondition;
    }

    private Order createOrder(String externalReference, OrderStatus status, BigDecimal total, PaymentCondition paymentCondition) {
        Order order = new Order();
        order.setId(orderId);
        order.setExternalReference(externalReference);
        order.setTenantCode(tenantCode);
        order.setStatus(status);
        order.setCode("ORDER-001");
        order.setTotal(total);
        order.setPaymentCondition(paymentCondition);

        Buyer buyer = new Buyer();
        buyer.setId(buyerId);
        buyer.setExternalReference("BUYER-REF-001");
        order.setBuyer(buyer);

        Seller seller = new Seller();
        seller.setId(sellerId);
        order.setSeller(seller);

        return order;
    }
}
