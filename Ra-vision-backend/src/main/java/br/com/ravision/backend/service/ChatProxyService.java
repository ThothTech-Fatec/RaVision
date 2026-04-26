package br.com.ravision.backend.service;

import br.com.ravision.backend.dto.ChatRequest;
import br.com.ravision.backend.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatProxyService {

    @Value("${ai.service.url:http://localhost:8001/api/chat}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public ChatProxyService() {
        this.restTemplate = new RestTemplate();
    }

    public ChatResponse getAiResponse(ChatRequest request, String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
            headers.set("Content-Type", "application/json");

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    aiServiceUrl,
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            return new ChatResponse("Erro ao conectar com o serviço de IA: " + e.getMessage());
        }
    }
}
