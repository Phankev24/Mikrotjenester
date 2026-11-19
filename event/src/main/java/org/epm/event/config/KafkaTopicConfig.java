package org.epm.event.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.epm.event.kafka.EventKafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic eventJoinedTopic() {
        return TopicBuilder.name(EventKafkaProducer.TOPIC).partitions(1).replicas(1).build();
    }
}
