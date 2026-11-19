package org.epm.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EP&M Event Service")
                        .version("1.0")
                        .description("Events, joining and leaving events, and overviews that combine users, vendors and events."));
    }
}
