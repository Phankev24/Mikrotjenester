package org.epm.event.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageConsumer {
    @KafkaListener(topics = "quickstart-events", groupId = "my-group")
    public void consumeMessage(String message){
        System.out.println("-> Consumed message asyncronously: " + message);
    }
}
