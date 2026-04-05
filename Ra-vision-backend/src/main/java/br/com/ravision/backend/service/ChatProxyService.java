package br.com.ravision.backend.service;

import br.com.ravision.backend.dto.ChatRequest;
import br.com.ravision.backend.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatProxyService {

    @Value("${ai.service.url:http://localhost:8001/api/chat}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public ChatProxyService() {
        this.restTemplate = new RestTemplate();
    }

    public ChatResponse getAiResponse(ChatRequest request) {
        try {
            return restTemplate.postForObject(aiServiceUrl, request, ChatResponse.class);
        } catch (Exception e) {
            return new ChatResponse("Erro ao conectar com o serviço de IA: " + e.getMessage());
        }
    }
}
