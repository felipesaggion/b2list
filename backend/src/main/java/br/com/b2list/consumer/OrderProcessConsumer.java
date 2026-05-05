package br.com.b2list.consumer;

import br.com.b2list.config.RabbitMQConfig;
import br.com.b2list.event.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private static final int MAX_RETRIES = 3;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PROCESS_QUEUE)
    public void processOrderEvent(Message message, Channel channel) throws IOException {
        String eventJson = new String(message.getBody());
        MessageProperties messageProperties = message.getMessageProperties();
        Map<String, Object> headers = message.getMessageProperties().getHeaders();

        String tenant = (String) headers.get("x-tenant");
        String eventType = (String) headers.get("x-event-type");
        String correlationId = (String) headers.get("x-correlation-id");
        Integer retryCount = (Integer) headers.getOrDefault("x-retry-count", 0);

        try {
            OrderEvent orderEvent = objectMapper.readValue(eventJson, OrderEvent.class);
            log.info("CONSUMER [order.process] - Tenant: {}, EventType: {}, CorrelationId: {}, RetryCount: {}, OrderId: {}",
                    tenant, eventType, correlationId, retryCount, orderEvent.getPayload().getOrderId());

            if (shouldFailProcessing(retryCount)) {
                throw new RuntimeException("Simulando falha no processamento do pedido " + orderEvent.getPayload().getOrderId());
            }

            log.info("CONSUMER [order.process] - Pedido {} processado com SUCESSO. Order ID: {}", eventType, orderEvent.getPayload().getOrderId());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // Confirma o processamento
        } catch (Exception e) {
            log.error("CONSUMER [order.process] - Erro ao processar evento. Tenant: {}, EventType: {}, CorrelationId: {}, RetryCount: {}. Erro: {}",
                    tenant, eventType, correlationId, retryCount, e.getMessage());

            if (retryCount >= MAX_RETRIES) {
                log.warn("CONSUMER [order.process] - Máximo de retries atingido para o evento. Enviando para Parking Lot. Order ID: {}",
                        extractOrderIdFromJson(eventJson));
                sendToParkingLot(message, channel);
            } else {
                log.warn("CONSUMER [order.process] - Rejeitando mensagem para DLQ (retry). Order ID: {}",
                        extractOrderIdFromJson(eventJson));

                MessageProperties newMessageProperties = new MessageProperties();
                headers.forEach(newMessageProperties::setHeader);
                newMessageProperties.setHeader("x-retry-count", retryCount + 1);

                Message retryMessage = MessageBuilder.withBody(message.getBody())
                        .andProperties(newMessageProperties)
                        .build();

                rabbitTemplate.send("", RabbitMQConfig.ORDER_PROCESS_DLQ, retryMessage);

                channel.basicAck(messageProperties.getDeliveryTag(), false);
            }
        }
    }

    private boolean shouldFailProcessing(int retryCount) {
        return retryCount < 2 || random.nextBoolean();
    }

    private void sendToParkingLot(Message originalMessage, Channel channel) throws IOException {
        Message parkingLotMessage = MessageBuilder.withBody(originalMessage.getBody())
                .andProperties(originalMessage.getMessageProperties())
                .build();

        rabbitTemplate.send("", RabbitMQConfig.ORDER_PROCESS_PARKING_LOT, parkingLotMessage);

        channel.basicAck(originalMessage.getMessageProperties().getDeliveryTag(), false);
    }

    private UUID extractOrderIdFromJson(String eventJson) {
        try {
            OrderEvent event = objectMapper.readValue(eventJson, OrderEvent.class);
            return event.getPayload().getOrderId();
        } catch (Exception e) {
            log.error("Erro ao extrair OrderId do JSON: {}", eventJson, e);
            return null;
        }
    }
}