package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.MonitoramentoAgregadoDTO;
import br.com.ravision.backend.dto.MonitoramentoIADTO;
import br.com.ravision.backend.service.MonitoramentoIAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/monitoramento/ia")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonitoramentoIAController {

    private final MonitoramentoIAService monitoramentoIAService;

    @Value("${ravision.ai.apikey:RAVISION_SECURE_API_KEY_2026}")
    private String expectedApiKey;

    @PostMapping
    public ResponseEntity<Void> ingerirMetricas(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                                @RequestBody MonitoramentoIADTO metrica) {
        
        // Validação da API Key para Server-to-Server
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            log.warn("Tentativa não autorizada de ingestão de métricas de IA. Key fornecida: {}", apiKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        monitoramentoIAService.salvarMetrica(metrica);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CTO', 'ADMINISTRADOR')")
    public ResponseEntity<MonitoramentoAgregadoDTO> buscarEstatisticas(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        log.info("Buscando estatísticas de Monitoramento IA entre {} e {}", dataInicio, dataFim);
        MonitoramentoAgregadoDTO dados = monitoramentoIAService.buscarMetricasAgregadas(dataInicio, dataFim);
        
        return ResponseEntity.ok(dados);
    }
}
