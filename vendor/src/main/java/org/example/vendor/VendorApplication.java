package org.example.vendor;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8100", description = "API Gateway")
        }
)
@SpringBootApplication
public class VendorApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorApplication.class, args);
    }

}
