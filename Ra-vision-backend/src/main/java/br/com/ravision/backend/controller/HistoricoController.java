package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.HistoricoResponse;
import br.com.ravision.backend.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirRegistro(@PathVariable Long id) {
        historicoService.excluirPorId(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> limparHistorico() {
        historicoService.limparTodos();
        return ResponseEntity.noContent().build();
    }
}
