package br.com.b2list.consumer;

import br.com.b2list.config.RabbitMQConfig;
import br.com.b2list.event.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationConsumer {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATION_QUEUE)
    public void handleNotificationEvent(Message message, Channel channel) throws IOException {
        String eventJson = new String(message.getBody());
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        String tenant = (String) headers.get("x-tenant");
        String eventType = (String) headers.get("x-event-type");
        String correlationId = (String) headers.get("x-correlation-id");

        try {
            OrderEvent orderEvent = objectMapper.readValue(eventJson, OrderEvent.class);
            log.info("CONSUMER [order.notification] - Recebido evento de notificação. Tenant: {}, EventType: {}, CorrelationId: {}, OrderId: {}",
                    tenant, eventType, correlationId, orderEvent.getPayload().getOrderId());

            log.info("CONSUMER [order.notification] - Notificação enviada para o pedido {}. Status: {}",
                    orderEvent.getPayload().getOrderId(), orderEvent.getPayload().getStatus());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // Confirma o processamento
        } catch (Exception e) {
            log.error("CONSUMER [order.notification] - Erro ao processar evento de notificação. Tenant: {}, EventType: {}, CorrelationId: {}. Erro: {}",
                    tenant, eventType, correlationId, e.getMessage(), e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}