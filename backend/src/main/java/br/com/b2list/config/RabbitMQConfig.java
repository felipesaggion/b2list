package br.com.b2list.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EVENTS_FANOUT_EXCHANGE = "order.events.fanout";

    public static final String ORDER_PROCESS_QUEUE = "order.process.queue";
    public static final String ORDER_PROCESS_DLQ = "order.process.dlq";
    public static final String ORDER_PROCESS_PARKING_LOT = "order.process.parking-lot";

    public static final String ORDER_NOTIFICATION_QUEUE = "order.notification.queue";

    @Value("${rabbitmq.dlq.ttl:30000}")
    private Long dlqTtl;

    @Value("${rabbitmq.parking-lot.ttl:2592000000}") // Default 30 dias (30 * 24 * 60 * 60 * 1000 ms)
    private Long parkingLotTtl;

    @Bean
    public FanoutExchange orderEventsFanoutExchange() {
        return new FanoutExchange(ORDER_EVENTS_FANOUT_EXCHANGE);
    }
    @Bean
    public Queue orderProcessQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", ORDER_PROCESS_DLQ);
        return new Queue(ORDER_PROCESS_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue orderProcessDlq() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", ORDER_PROCESS_QUEUE);
        args.put("x-message-ttl", dlqTtl);
        return new Queue(ORDER_PROCESS_DLQ, true, false, false, args);
    }

    @Bean
    public Queue orderProcessParkingLotQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", parkingLotTtl);
        return new Queue(ORDER_PROCESS_PARKING_LOT, true, false, false, args);
    }

    @Bean
    public Queue orderNotificationQueue() {
        return new Queue(ORDER_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding bindOrderProcessQueueToFanout() {
        return BindingBuilder.bind(orderProcessQueue()).to(orderEventsFanoutExchange());
    }

    @Bean
    public Binding bindOrderNotificationQueueToFanout() {
        return BindingBuilder.bind(orderNotificationQueue()).to(orderEventsFanoutExchange());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}