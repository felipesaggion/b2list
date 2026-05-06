package br.com.b2list.util;

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
import br.com.b2list.origin.OriginContext;
import br.com.b2list.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.ResponseEntity;

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
                    "Order não encontrado pelo external reference: " + externalReference,
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

    public static ResponseEntity<ErrorResponseDTO> checkIfOrderIsAlreadyCanceled(String externalReference, Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            ErrorResponseDTO errorResponseDTO = ErrorUtil.buildErrorResponse(Error.ORD_STATUS_001);
            errorResponseDTO.setDetails(List.of(
                    "O pedido com referência " + externalReference + " já foi cancelado anteriormente"
            ));
            log.error(ToStringBuilder.reflectionToString(errorResponseDTO, ToStringStyle.MULTI_LINE_STYLE));
            return ResponseEntity.badRequest().body(errorResponseDTO);
        }
        return null;
    }

    /**
     * Returns the default currency for money values (BRL).
     */
    public static Currency getDefaultCurrency() {
        return Currency.getInstance("BRL");
    }
}
