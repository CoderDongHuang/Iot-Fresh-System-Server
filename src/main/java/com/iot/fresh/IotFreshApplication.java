package com.iot.fresh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IotFreshApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotFreshApplication.class, args);
    }

}