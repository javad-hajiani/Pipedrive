package com.test.demo;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author amir
 */
@Configuration
public class Configurations {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
