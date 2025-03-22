package com.reportai.reportaiserver.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

   @Bean
   public RestTemplate restTemplate(RestTemplateBuilder builder) {
      return builder
              .setConnectTimeout(Duration.ofSeconds(30))
              .setReadTimeout(Duration.ofSeconds(60))
              .additionalInterceptors((request, body, execution) -> {
                 // é possível inserir um interceptor aqui
                 return execution.execute(request, body);
              })
              .build();
   }
}
