package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.HistoricoResponse;
import br.com.ravision.backend.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService historicoService;

    @GetMapping
    public ResponseEntity<List<HistoricoResponse>> listarHistorico() {
        return ResponseEntity.ok(historicoService.listarTodos());
    }
}
