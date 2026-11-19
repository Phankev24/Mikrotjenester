package org.epm.event.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventKafkaProducer {
    public static final String TOPIC = "event-joined";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishEventJoined(EventJoinedEvent payload){
        String json = objectMapper.writeValueAsString(payload);
        String key = payload.externalEventId().toString();
        kafkaTemplate.send(TOPIC, key, json).whenComplete((result, error) -> {
            if (error != null) {
                log.error("Failed to publish event-joined message for event {} to topic {}", key, TOPIC, error);
            } else {
                log.info("Published event-joined message for event {} to topic {}", key, TOPIC);
            }
        });
    }
}
