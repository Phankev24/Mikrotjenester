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
        kafkaTemplate.send(TOPIC, payload.externalEventId().toString(), json);
        log.info("Published event-joined notification to kafka: {}", json);
    }
}
