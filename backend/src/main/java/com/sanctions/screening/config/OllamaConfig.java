package com.sanctions.screening.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.timeout-seconds:120}")
    private int timeoutSeconds;

    @Bean
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        httpClient -> httpClient
                                .responseTimeout(Duration.ofSeconds(timeoutSeconds))
                                .connectTimeout(Duration.ofSeconds(30))
                ))
                .build();
    }

    @Bean
    public OllamaProperties ollamaProperties() {
        return new OllamaProperties(baseUrl, timeoutSeconds);
    }

    public record OllamaProperties(String baseUrl, int timeoutSeconds) {}
}
