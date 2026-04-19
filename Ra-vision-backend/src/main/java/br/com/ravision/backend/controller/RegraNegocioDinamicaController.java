package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.RegraNegocioRequest;
import br.com.ravision.backend.dto.RegraNegocioResponse;
import br.com.ravision.backend.service.RegraNegocioDinamicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regras")
@RequiredArgsConstructor
public class RegraNegocioDinamicaController {

    private final RegraNegocioDinamicaService service;

    @GetMapping
    public ResponseEntity<List<RegraNegocioResponse>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegraNegocioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RegraNegocioResponse> criarRegra(@RequestBody @Valid RegraNegocioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarRegra(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraNegocioResponse> editarRegra(@PathVariable Long id, @RequestBody @Valid RegraNegocioRequest request) {
        return ResponseEntity.ok(service.editarRegra(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirRegra(@PathVariable Long id) {
        service.excluirRegra(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('GESTOR_RH')")
    public ResponseEntity<RegraNegocioResponse> aprovarRegra(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprovarRegra(id));
    }

    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('GESTOR_RH')")
    public ResponseEntity<RegraNegocioResponse> recusarRegra(@PathVariable Long id) {
        return ResponseEntity.ok(service.recusarRegra(id));
    }
}
