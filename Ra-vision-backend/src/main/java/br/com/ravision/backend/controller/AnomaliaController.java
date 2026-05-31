package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.ConfiguracaoAnomalia;
import br.com.ravision.backend.domain.StatusAnaliseAnomalia;
import br.com.ravision.backend.domain.TipoAnomalia;
import br.com.ravision.backend.dto.AnomaliaDTO;
import br.com.ravision.backend.dto.AuditarAnomaliaDTO;
import br.com.ravision.backend.dto.ConfiguracaoAnomaliaDTO;
import br.com.ravision.backend.service.AnomaliaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/anomalias")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMINISTRADOR')")
public class AnomaliaController {

    private final AnomaliaService anomaliaService;

    @PostMapping("/configuracao")
    public ResponseEntity<ConfiguracaoAnomalia> configurar(@RequestBody ConfiguracaoAnomaliaDTO dto) {
        log.info("Recebida requisição para atualizar configuração de anomalias: {}", dto);
        ConfiguracaoAnomalia config = anomaliaService.salvarConfiguracao(dto);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/configuracao")
    public ResponseEntity<ConfiguracaoAnomalia> buscarConfiguracao() {
        return ResponseEntity.ok(anomaliaService.obterConfiguracaoAtiva());
    }

    @PostMapping("/disparar")
    public ResponseEntity<Map<String, Object>> dispararAnalise(@RequestParam("dateRef") String dateRefStr) {
        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> resultado = anomaliaService.dispararDeteccaoAnomalias(dateRef);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro interno ao disparar anomalias", e);
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro interno ao processar anomalias."));
        }
    }

    @GetMapping
    public ResponseEntity<Page<AnomaliaDTO>> buscarAnomalias(
            @RequestParam(value = "dateRef", required = false) String dateRefStr,
            @RequestParam(value = "matricula", required = false) String matricula,
            @RequestParam(value = "tipoAnomalia", required = false) TipoAnomalia tipoAnomalia,
            @RequestParam(value = "statusAnalise", required = false) StatusAnaliseAnomalia statusAnalise,
            Pageable pageable) {
        
        LocalDate dateRef = null;
        if (dateRefStr != null && !dateRefStr.isEmpty()) {
            dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        if (matricula != null && matricula.trim().isEmpty()) {
            matricula = null;
        }

        Page<AnomaliaDTO> page = anomaliaService.buscarAnomalias(dateRef, matricula, tipoAnomalia, statusAnalise, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}/auditar")
    public ResponseEntity<?> auditarAnomalia(@PathVariable Long id, @RequestBody AuditarAnomaliaDTO dto) {
        try {
            anomaliaService.auditarAnomalia(id, dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
