package com.test.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Main configuration class.
 */
@Configuration
public class Configurations {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
