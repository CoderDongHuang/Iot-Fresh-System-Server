package com.iot.fresh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IoT Fresh 2022 API")
                        .version("1.0")
                        .description("智能鲜品物联网系统API文档")
                        .contact(new Contact()
                                .name("IoT Fresh Team")
                                .email("iot-fresh@example.com")
                        )
                );
    }
}