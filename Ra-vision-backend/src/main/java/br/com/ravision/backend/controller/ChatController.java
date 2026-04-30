package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.ChatRequest;
import br.com.ravision.backend.dto.ChatResponse;
import br.com.ravision.backend.service.ChatProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Mantém flexível para o dev, pode ser restrito depois
public class ChatController {

    @Autowired
    private ChatProxyService chatProxyService;

    @PostMapping
    public ChatResponse processChat(@RequestBody ChatRequest request, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return chatProxyService.getAiResponse(request, authHeader);
    }
}
