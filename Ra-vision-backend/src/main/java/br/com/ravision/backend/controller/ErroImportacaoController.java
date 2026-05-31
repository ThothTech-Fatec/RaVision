package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.StatusErro;
import br.com.ravision.backend.dto.ErroImportacaoDTO;
import br.com.ravision.backend.service.ErroImportacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/erros-importacao")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMINISTRADOR')")
public class ErroImportacaoController {

    private final ErroImportacaoService service;

    @GetMapping
    public ResponseEntity<Page<ErroImportacaoDTO>> listarErros(
            @RequestParam(required = false) StatusErro status,
            @RequestParam(required = false) String nomeArquivo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Pageable pageable) {
        return ResponseEntity.ok(service.listarErros(status, nomeArquivo, dataInicio, dataFim, pageable));
    }

    @PutMapping("/{id}/resolver")
    public ResponseEntity<ErroImportacaoDTO> marcarComoResolvido(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoResolvido(id));
    }

    @GetMapping("/pendentes/count")
    public ResponseEntity<Long> contarErrosPendentes() {
        return ResponseEntity.ok(service.contarErrosPendentes());
    }
}
