package br.com.b2list.service.impl;

import br.com.b2list.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotServiceImpl implements ParkingLotService {

    private final RabbitTemplate rabbitTemplate;

    public int reprocessMessages(String parkingLotQueueName, int maxMessages) {
        int reprocessedCount = 0;

        if (!parkingLotQueueName.endsWith(".parking-lot")) {
            log.warn("Tentativa de reprocessar uma fila que não é de parking lot: {}", parkingLotQueueName);
            return 0;
        }

        String mainQueueName = parkingLotQueueName.replace(".parking-lot", ".queue");
        if (mainQueueName.equals(parkingLotQueueName)) { // Se a substituição não ocorreu, algo está errado
            log.error("Não foi possível derivar a fila principal para o parking lot: {}", parkingLotQueueName);
            return 0;
        }

        log.info("Iniciando reprocessamento de até {} mensagens da fila {} para a fila principal {}",
                maxMessages, parkingLotQueueName, mainQueueName);

        for (int i = 0; i < maxMessages; i++) {
            Message message = rabbitTemplate.receive(parkingLotQueueName);

            if (message == null) {
                log.info("Fila {} vazia ou limite de mensagens atingido.", parkingLotQueueName);
                break;
            }

            try {
                MessageProperties originalProperties = message.getMessageProperties();
                Map<String, Object> originalHeaders = originalProperties.getHeaders();

                MessageProperties newProperties = new MessageProperties();
                originalHeaders.forEach(newProperties::setHeader);

                newProperties.setHeader("x-retry-count", 0);
                newProperties.setHeader("x-reprocessed-at", System.currentTimeMillis());
                newProperties.setHeader("x-original-parking-lot", parkingLotQueueName);


                Message reprocessedMessage = MessageBuilder.withBody(message.getBody())
                        .andProperties(newProperties)
                        .build();

                rabbitTemplate.send("", mainQueueName, reprocessedMessage);

                log.info("Mensagem reprocessada com sucesso do parking lot {} para a fila principal {}. CorrelationId: {}",
                        parkingLotQueueName, mainQueueName, originalHeaders.get("x-correlation-id"));
                reprocessedCount++;
            } catch (Exception e) {
                log.error("Erro ao reprocessar mensagem do parking lot {}. CorrelationId: {}. Erro: {}",
                        parkingLotQueueName, Optional.ofNullable(message.getMessageProperties().getHeaders().get("x-correlation-id")).orElse("N/A"), e.getMessage(), e);
                MessageProperties failedReprocessProperties = new MessageProperties();
                message.getMessageProperties().getHeaders().forEach(failedReprocessProperties::setHeader); // Copia os headers originais
                failedReprocessProperties.setHeader("x-reprocess-failed", true);
                failedReprocessProperties.setHeader("x-reprocess-failure-reason", e.getMessage());
                failedReprocessProperties.setHeader("x-reprocess-failed-at", System.currentTimeMillis());

                Message failedReprocessMessage = MessageBuilder.withBody(message.getBody())
                        .andProperties(failedReprocessProperties)
                        .build();

                rabbitTemplate.send("", parkingLotQueueName, failedReprocessMessage);
                log.warn("Mensagem que falhou no reprocessamento foi devolvida ao parking lot {}. CorrelationId: {}",
                        parkingLotQueueName, Optional.ofNullable(message.getMessageProperties().getHeaders().get("x-correlation-id")).orElse("N/A"));
            }
        }

        log.info("Reprocessamento concluído. Total de {} mensagens reprocessadas da fila {}.", reprocessedCount, parkingLotQueueName);
        return reprocessedCount;
    }
}