package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.DashboardAgregacaoDTO;
import br.com.ravision.backend.dto.DashboardTotalDTO;
import br.com.ravision.backend.dto.ExecutivaKPIsDTO;
import br.com.ravision.backend.dto.HistoricoEvolucaoDTO;
import br.com.ravision.backend.dto.RankingVendedorDTO;
import br.com.ravision.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboards/comissoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMINISTRADOR')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/loja")
    public ResponseEntity<List<DashboardAgregacaoDTO>> getComissoesPorLoja(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getComissoesPorLoja(dateRef));
    }

    @GetMapping("/marca")
    public ResponseEntity<List<DashboardAgregacaoDTO>> getComissoesPorMarca(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getComissoesPorMarca(dateRef));
    }

    @GetMapping("/geral")
    public ResponseEntity<DashboardTotalDTO> getTotalComissaoGeral(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getTotalComissaoGeral(dateRef));
    }

    @GetMapping("/executiva/kpis")
    public ResponseEntity<ExecutivaKPIsDTO> getKPIsExecutiva(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getKPIsExecutiva(dateRef));
    }

    @GetMapping("/executiva/historico")
    public ResponseEntity<List<HistoricoEvolucaoDTO>> getHistoricoExecutiva(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getHistoricoExecutiva(dateRef));
    }

    @GetMapping("/executiva/top-marcas")
    public ResponseEntity<List<DashboardAgregacaoDTO>> getTopMarcas(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getTopMarcas(dateRef));
    }

    @GetMapping("/lojas/ranking-vendedores")
    public ResponseEntity<List<RankingVendedorDTO>> getRankingVendedores(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getRankingVendedores(dateRef));
    }

    @GetMapping("/rh/proporcionalidade")
    public ResponseEntity<List<DashboardAgregacaoDTO>> getProporcionalidadeRH(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getProporcionalidadeRH(dateRef));
    }

    @GetMapping("/rh/anomalias-cadastro")
    public ResponseEntity<Long> getAnomaliasCadastroRH(
            @RequestParam("dateRef") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRef) {
        return ResponseEntity.ok(dashboardService.getAnomaliasCadastroRH(dateRef));
    }
}
