package br.com.b2list.util;

import br.com.b2list.domain.dto.BuyerDTO;
import br.com.b2list.domain.dto.ErrorResponseDTO;
import br.com.b2list.domain.dto.ItemDTO;
import br.com.b2list.domain.dto.OrderDTO;
import br.com.b2list.domain.dto.OrderItemDTO;
import br.com.b2list.domain.dto.OrderRequestDTO;
import br.com.b2list.domain.dto.OrderResponseDTO;
import br.com.b2list.domain.dto.OrderResult;
import br.com.b2list.domain.dto.PaymentConditionDTO;
import br.com.b2list.domain.dto.SellerDTO;
import br.com.b2list.domain.dto.WarehouseDTO;
import br.com.b2list.domain.entity.Buyer;
import br.com.b2list.domain.entity.Order;
import br.com.b2list.domain.entity.OrderItem;
import br.com.b2list.domain.entity.PaymentCondition;
import br.com.b2list.domain.entity.ProductPrice;
import br.com.b2list.domain.entity.Seller;
import br.com.b2list.domain.entity.Warehouse;
import br.com.b2list.enums.Error;
import br.com.b2list.enums.OrderOrigin;
import br.com.b2list.enums.OrderStatus;
import br.com.b2list.origin.OriginContext;
import br.com.b2list.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static br.com.b2list.enums.Error.ORD_VALIDATION_006;

@Slf4j
public class OrderUtils {

    public static OrderOrigin getOrderOrigin() {
        OrderOrigin orderOrigin;
        try {
            String originString = OriginContext.getOrigin();
            orderOrigin = OrderOrigin.valueOf(originString);
        } catch (Exception ex) {
            orderOrigin = OrderOrigin.API;
            log.warn("Origin não presente no header(x-origin) ou valor não é valido. Definindo origin como API");
        }
        return orderOrigin;
    }

    public static OrderResponseDTO populateOrderResponseDTO(Order orderSaved, PaymentCondition paymentCondition, OrderResult orderResult) {
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setCode(orderSaved.getCode());
        orderResponseDTO.setMessage("Pedido criado com sucesso");

        OrderResponseDTO.ValidationDTO validationDTO = new OrderResponseDTO.ValidationDTO();
        validationDTO.setWarnings(List.of());

        OrderResponseDTO.PricingDTO pricingDTO = new OrderResponseDTO.PricingDTO();
        pricingDTO.setSubtotal(orderSaved.getSubtotal());
        pricingDTO.setDescription("Precificação aplicada: " + paymentCondition.getDescription());

        OrderResponseDTO.DataDTO dataDTO = new OrderResponseDTO.DataDTO();
        dataDTO.setOrderId(orderSaved.getId().toString());
        dataDTO.setExternalReference(orderSaved.getExternalReference());
        dataDTO.setStatus(orderSaved.getStatus().name());
        dataDTO.setSubtotal(orderSaved.getSubtotal());
        dataDTO.setDiscountValue(orderSaved.getSubtotal().subtract(orderSaved.getTotal()));
        dataDTO.setTotal(orderSaved.getTotal());
        dataDTO.setItemCount(orderSaved.getItems().size());
        dataDTO.setValidation(validationDTO);
        dataDTO.setPricing(pricingDTO);
        dataDTO.setDiscount(orderResult.getDiscountDTO());

        orderResponseDTO.setData(dataDTO);
        return orderResponseDTO;
    }

    public static ResponseEntity<ErrorResponseDTO> validateBuyersCredit(Order order, Buyer buyer) {
        if (order.getTotal().compareTo(buyer.getCreditLimit()) > 0) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_003);
            List<String> details = List.of(
                    "Crédito disponível: " + getDefaultCurrency().getSymbol() + " " + order.getTotal(),
                    "Valor do pedido: " + getDefaultCurrency().getSymbol() + " " + buyer.getCreditLimit()
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfExternalReferenceIsPresent(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getExternalReference() == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "External reference não pode ser nulo"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfPaymentConditionIsPresent(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getPaymentConditionCode() == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "PaymentConditionCode não pode ser nulo"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfWarehouseIsPresent(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getWarehouseReference() == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "Warehouse reference não pode ser nulo"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfOrderIsPresent(Order order, String externalReference) {
        if (order == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "Order não encontrado pelo external reference: " +externalReference,
                    "Ou order não pertence ao tenant: " + TenantContext.getTenant()
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfSellerIsPresent(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getSellerReference() == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "Seller reference não pode ser nulo"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfBuyerIsPresent(OrderRequestDTO orderRequestDTO) {
        if (orderRequestDTO.getBuyerReference() == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(ORD_VALIDATION_006);
            List<String> details = List.of(
                    "Buyer reference não pode ser nulo"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> validateProductPrice(ItemDTO itemDTO, ProductPrice productPrice) {
        if (productPrice == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_004);
            List<String> details = List.of(
                    "Product Code " + itemDTO.getProductCode() + " não encontrado",
                    "Product Code inativo, pertence a outro tenant ou não esta no warehouse selecionado"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> validatePaymentCondition(OrderRequestDTO orderRequestDTO, PaymentCondition paymentCondition) {
        if (paymentCondition == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_002);
            List<String> details = List.of(
                    "PaymentCondition com code " + orderRequestDTO.getPaymentConditionCode() + " não encontrado",
                    "PaymentCondition inativo ou pertence a outro tenant"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> validateWarehouse(OrderRequestDTO orderRequestDTO, Warehouse warehouse, Seller seller) {
        if (warehouse == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_002);
            List<String> details = List.of(
                    "Warehouse com external reference " + orderRequestDTO.getWarehouseReference() + " não encontrado",
                    "Warehouse inativo ou pertence a outro tenant",
                    "Warehouse com dependencia em seller id " + seller.getId() + " não encontrado"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> validateSeller(OrderRequestDTO orderRequestDTO, Seller seller) {
        if (seller == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_002);
            List<String> details = List.of(
                    "Seller com external reference " + orderRequestDTO.getSellerReference() + " não encontrado",
                    "Seller inativo ou pertence a outro tenant"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> validateBuyer(OrderRequestDTO orderRequestDTO, Buyer buyer) {
        if (buyer == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_002);
            List<String> details = List.of(
                    "Buyer com external reference " + orderRequestDTO.getBuyerReference() + " não encontrado",
                    "Buyer inativo ou pertence a outro tenant"
            );
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.unprocessableEntity().body(errorResponseDTO);
        }
        return null;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfTenantIsPresent(String tenant) {
        if (tenant == null) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_VALIDATION_005);
            List<String> details = List.of("Tenant code não esta presente no header(x-tenant)");
            errorResponseDTO.setDetails(details);
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.badRequest().body(errorResponseDTO);
        }
        return null;
    }

    public static BuyerDTO createBuyerDTO(Order order) {
        BuyerDTO buyerDTO = new BuyerDTO();
        buyerDTO.setExternalReference(order.getBuyer().getExternalReference());
        buyerDTO.setName(order.getBuyer().getName());
        buyerDTO.setCreditLimit(order.getBuyer().getCreditLimit());
        buyerDTO.setTenantCode(order.getBuyer().getTenantCode());
        buyerDTO.setEnabled(order.getBuyer().isEnabled());
        buyerDTO.setCreatedAt(order.getBuyer().getCreatedAt());
        buyerDTO.setLastModified(order.getBuyer().getLastModified());
        buyerDTO.setVersion(order.getBuyer().getVersion());
        return buyerDTO;
    }

    public static SellerDTO createSellerDTO(Order order) {
        SellerDTO sellerDTO = new SellerDTO();
        sellerDTO.setExternalReference(order.getSeller().getExternalReference());
        sellerDTO.setName(order.getSeller().getName());
        sellerDTO.setTenantCode(order.getSeller().getTenantCode());
        sellerDTO.setEnabled(order.getSeller().isEnabled());
        sellerDTO.setCreatedAt(order.getSeller().getCreatedAt());
        return sellerDTO;
    }

    public static WarehouseDTO createWarehouseDTO(Order order) {
        WarehouseDTO warehouseDTO = new WarehouseDTO();
        warehouseDTO.setExternalReference(order.getWarehouse().getExternalReference());
        warehouseDTO.setName(order.getWarehouse().getName());
        warehouseDTO.setTenantCode(order.getWarehouse().getTenantCode());
        warehouseDTO.setEnabled(order.getWarehouse().isEnabled());
        warehouseDTO.setCreatedAt(order.getWarehouse().getCreatedAt());
        return warehouseDTO;
    }

    public static PaymentConditionDTO createPaymentConditionDTO(Order order) {
        PaymentConditionDTO paymentConditionDTO = new PaymentConditionDTO();
        paymentConditionDTO.setTenantCode(order.getPaymentCondition().getTenantCode());
        paymentConditionDTO.setCode(order.getPaymentCondition().getCode());
        paymentConditionDTO.setDescription(order.getPaymentCondition().getDescription());
        paymentConditionDTO.setMinOrderValue(order.getPaymentCondition().getMinOrderValue());
        paymentConditionDTO.setMaxItems(order.getPaymentCondition().getMaxItems());
        paymentConditionDTO.setAllowBonusOrder(order.getPaymentCondition().getAllowBonusOrder());
        paymentConditionDTO.setBusinessHoursOnly(order.getPaymentCondition().getBusinessHoursOnly());
        paymentConditionDTO.setOperationalFeePercent(order.getPaymentCondition().getOperationalFeePercent());
        paymentConditionDTO.setDiscountPercent(order.getPaymentCondition().getDiscountPercent());
        paymentConditionDTO.setDiscountExtraPercent(order.getPaymentCondition().getDiscountExtraPercent());
        paymentConditionDTO.setDiscountCondition(order.getPaymentCondition().getDiscountCondition());
        paymentConditionDTO.setFreeShippingThreshold(order.getPaymentCondition().getFreeShippingThreshold());
        paymentConditionDTO.setAlwaysFreeShipping(order.getPaymentCondition().getAlwaysFreeShipping());
        paymentConditionDTO.setEnabled(order.getPaymentCondition().getEnabled());
        return paymentConditionDTO;
    }

    public static List<OrderItemDTO> createListOfItemsDTO(Order order) {
        List<OrderItemDTO> items = new ArrayList<>();
        for (OrderItem oi : order.getItems()) {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            orderItemDTO.setProductCode(oi.getProductCode());
            orderItemDTO.setProductName(oi.getProductName());
            orderItemDTO.setQuantity(oi.getQuantity());
            orderItemDTO.setUnitPrice(oi.getUnitPrice());
            orderItemDTO.setListPrice(oi.getListPrice());
            orderItemDTO.setSubtotal(oi.getSubtotal());
            items.add(orderItemDTO);
        }
        return items;
    }

    public static ResponseEntity<ErrorResponseDTO> checkIfOrderIsAlreadyCanceled(String externalReference, Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            ErrorResponseDTO errorResponseDTO  = ErrorUtil.buildErrorResponse(Error.ORD_STATUS_001);
            errorResponseDTO.setDetails(List.of(
                    "O pedido com referência " + externalReference + " já foi cancelado anteriormente"
            ));
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.badRequest().body(errorResponseDTO);
        }
        return null;
    }

    public static OrderDTO convertOrderToOrderDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setCode(order.getCode());
        orderDTO.setExternalReference(order.getExternalReference());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setSubtotal(order.getSubtotal());
        orderDTO.setDiscountValue(order.getDiscountValue());
        orderDTO.setTotal(order.getTotal());
        orderDTO.setOrigin(order.getOrigin());
        orderDTO.setTenantCode(order.getTenantCode());
        orderDTO.setCreatedAt(order.getCreatedAt());
        orderDTO.setLastModified(order.getLastModified());
        orderDTO.setVersion(order.getVersion());

        orderDTO.setBuyer(createBuyerDTO(order));
        orderDTO.setSeller(createSellerDTO(order));
        orderDTO.setWarehouse(createWarehouseDTO(order));
        orderDTO.setPaymentCondition(createPaymentConditionDTO(order));

        orderDTO.setItems(createListOfItemsDTO(order));
        return orderDTO;
    }

    /**
     * Returns the default currency for money values (BRL).
     */
    public static Currency getDefaultCurrency() {
        return Currency.getInstance("BRL");
    }
}
