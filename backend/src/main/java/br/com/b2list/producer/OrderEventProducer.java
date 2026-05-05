package br.com.b2list.producer;

import br.com.b2list.config.RabbitMQConfig;
import br.com.b2list.enums.EvenType;
import br.com.b2list.event.OrderEvent;
import br.com.b2list.event.OrderPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // Para converter o payload em String antes de enviar como header

    public void publishOrderCreatedEvent(OrderPayload payload, String tenantId, UUID correlationId) {
        publishOrderEvent(payload, tenantId, correlationId, EvenType.ORDER_CREATED);
    }

    public void publishOrderCancelledEvent(OrderPayload payload, String tenantId, UUID correlationId) {
        publishOrderEvent(payload, tenantId, correlationId, EvenType.ORDER_CANCELLED);
    }

    private void publishOrderEvent(OrderPayload payload, String tenantId, UUID correlationId, EvenType eventType) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(eventType)
                    .timestamp(OffsetDateTime.now())
                    .tenant(tenantId)
                    .correlationId(correlationId)
                    .payload(payload)
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("x-tenant", tenantId);
            messageProperties.setHeader("x-correlation-id", correlationId.toString());
            messageProperties.setHeader("x-event-type", eventType);
            messageProperties.setHeader("x-retry-count", 0); // Inicia com 0

            Message message = MessageBuilder.withBody(eventJson.getBytes())
                    .andProperties(messageProperties)
                    .build();

            rabbitTemplate.send(RabbitMQConfig.ORDER_EVENTS_FANOUT_EXCHANGE, "", message); // Routing key vazia para fanout
            log.info("Evento {} publicado com sucesso para o tenant {}. Order ID: {}", eventType, tenantId, payload.getOrderId());

        } catch (Exception e) {
            log.error("Erro ao publicar evento {}: {}", eventType, e.getMessage(), e);
        }
    }
}