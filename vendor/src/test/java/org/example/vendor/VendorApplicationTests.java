package org.example.vendor;

import org.example.vendor.client.EventClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.config.import=optional:consul:",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.kafka.admin.auto-create=false"
})
class VendorApplicationTests {

    @MockitoBean
    private EventClient eventClient;

    @Test
    void contextLoads() {
    }
}