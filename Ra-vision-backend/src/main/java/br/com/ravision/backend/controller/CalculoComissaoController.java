package br.com.ravision.backend.controller;


import br.com.ravision.backend.service.CalculoComissaoService;
import br.com.ravision.backend.service.SimulacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalculoComissaoController {

    private final CalculoComissaoService service;
    private final SimulacaoService simulacaoService;

    @GetMapping("/base")
    public ResponseEntity<java.util.List<br.com.ravision.backend.domain.ComissaoCalculadaBase>> buscarCalculos(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return ResponseEntity.ok(service.buscarPorCompetencia(dateRef));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/base")
    public ResponseEntity<String> calcularBase(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            service.calcularCompetencia(dateRef);
            return ResponseEntity.ok("Calculos processados com sucesso para o mes " + dateRef);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao realizar calculo: " + e.getMessage());
        }
    }

    @GetMapping("/simular/regra/{id}")
    @PreAuthorize("hasRole('GESTOR_RH')")
    public ResponseEntity<?> simularImpacto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(simulacaoService.simular(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
