package org.epm.event.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaMessageController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC ="quickstart-events";

    public KafkaMessageController(KafkaTemplate<String, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/send")
    public String sendMessage(@RequestParam("message") String message){
        kafkaTemplate.send(TOPIC, message);
        return "Message sent to Kafka successfully: " + message;
    }
}
