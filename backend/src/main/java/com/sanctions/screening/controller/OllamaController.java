package com.sanctions.screening.controller;

import com.sanctions.screening.ollama.OllamaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/ollama")
@RequiredArgsConstructor
@Tag(name = "Ollama", description = "Ollama AI integration operations")
public class OllamaController {

    private final OllamaService ollamaService;

    @GetMapping("/health")
    @Operation(summary = "Check Ollama health", description = "Check if Ollama service is available")
    public Mono<ResponseEntity<Boolean>> checkHealth() {
        return ollamaService.checkOllamaHealth()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(false)));
    }

    @GetMapping("/models")
    @Operation(summary = "List available models", description = "List all available models in Ollama")
    public Mono<ResponseEntity<List<String>>> listModels() {
        return ollamaService.listAvailableModels()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(List.of())));
    }
}
