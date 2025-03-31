package com.duoc.app_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .filter((request, next) -> {
                System.out.println("Solicitud: " + request.method() + " " + request.url());
                request.headers().forEach((name, values) -> 
                    values.forEach(value -> System.out.println(name + ": " + value))
                );
    
                // Intenta imprimir el cuerpo si es posible
                if (request.body() != null) {
                    System.out.println("Cuerpo de la solicitud presente (no se puede mostrar directamente)");
                }
                
                return next.exchange(request);
            });
    }
}