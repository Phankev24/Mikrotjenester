package org.example.vendor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.vendor.kafka.VendorNotificationConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic eventJoinedTopic() {
        return TopicBuilder.name(VendorNotificationConsumer.EVENT_JOINED_TOPIC).partitions(1).replicas(1).build();
    }
}
