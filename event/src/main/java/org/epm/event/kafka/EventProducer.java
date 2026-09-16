//package org.epm.event.kafka;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EventProducer {
//    private static final String TOPIC = "event-created";
//
//    private final KafkaTemplate<String, String> kafkaTemplate;
//    private final ObjectMapper objectMapper;
//
//    public EventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper){
//        this.kafkaTemplate = kafkaTemplate;
//        this.objectMapper = objectMapper;
//    }
//}
