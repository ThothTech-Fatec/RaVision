package br.com.ravision.backend.controller;

import br.com.ravision.backend.service.AplicadorRegrasSazonaisService;
import br.com.ravision.backend.service.CalculoComissaoService;
import br.com.ravision.backend.service.CalculoProporcionalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrquestradorCalculoController {

    private final CalculoComissaoService calculoComissaoService;
    private final CalculoProporcionalService calculoProporcionalService;
    private final AplicadorRegrasSazonaisService aplicadorRegrasSazonaisService;

    @PostMapping("/processar-folha")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR_RH')")
    public ResponseEntity<String> processarFolhaCompleta(@RequestParam("dateRef") String dateRefStr) {
        log.info("Iniciando processamento em cascata da folha para a competencia: {}", dateRefStr);

        try {
            LocalDate dateRef = LocalDate.parse(dateRefStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            log.info("Passo 1/3: Calculando Base de Comissao...");
            calculoComissaoService.calcularCompetencia(dateRef);

            log.info("Passo 2/3: Calculando Comissao Proporcional (Leis e Faltas)...");
            calculoProporcionalService.calcularProporcional(dateRef);

            log.info("Passo 3/3: Aplicando Regras Sazonais (Auditoria e Fechamento Final)...");
            aplicadorRegrasSazonaisService.aplicarRegrasSazonais(dateRef);

            log.info("Processamento da Folha concluido com sucesso!");
            return ResponseEntity.ok("Fechamento de folha processado com sucesso para a competencia " + dateRefStr + ".");
        } catch (IllegalArgumentException e) {
            log.error("Erro de validacao ao processar a folha: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Erro de validacao: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro interno ao processar a folha", e);
            return ResponseEntity.internalServerError().body("Erro inesperado ao processar a folha: " + e.getMessage());
        }
    }
}
