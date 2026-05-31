package br.com.ravision.backend.service;

import br.com.ravision.backend.dto.DashboardAgregacaoDTO;
import br.com.ravision.backend.dto.DashboardTotalDTO;
import br.com.ravision.backend.repository.ComissaoCalculadaFinalRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.dto.ExecutivaKPIsDTO;
import br.com.ravision.backend.dto.HistoricoEvolucaoDTO;
import br.com.ravision.backend.dto.RankingVendedorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ComissaoCalculadaFinalRepository comissaoRepository;
    private final BaseVendasRepository baseVendasRepository;
    private final IntercorrenciaRHRepository intercorrenciaRHRepository;
    private final BaseRHRepository baseRHRepository;

    /**
     * Retorna o total de comissão por loja para um determinado mês.
     */
    public List<DashboardAgregacaoDTO> getComissoesPorLoja(LocalDate mesCompetencia) {
        log.info("Buscando comissões por loja para a competência: {}", mesCompetencia);
        return comissaoRepository.sumarizarComissoesPorLoja(mesCompetencia);
    }

    /**
     * Retorna o total de comissão por marca para um determinado mês.
     */
    public List<DashboardAgregacaoDTO> getComissoesPorMarca(LocalDate mesCompetencia) {
        log.info("Buscando comissões por marca para a competência: {}", mesCompetencia);
        return comissaoRepository.sumarizarComissoesPorMarca(mesCompetencia);
    }

    /**
     * Retorna o total geral de comissões para um determinado mês.
     */
    public DashboardTotalDTO getTotalComissaoGeral(LocalDate mesCompetencia) {
        log.info("Buscando total geral de comissões para a competência: {}", mesCompetencia);
        BigDecimal total = comissaoRepository.sumarizarComissaoGeral(mesCompetencia);
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        return new DashboardTotalDTO(total);
    }

    public ExecutivaKPIsDTO getKPIsExecutiva(LocalDate mesCompetencia) {
        log.info("Buscando KPIs Executiva para a competência: {}", mesCompetencia);
        BigDecimal faturamento = baseVendasRepository.sumarizarFaturamentoGeral(mesCompetencia);
        BigDecimal comissoes = comissaoRepository.sumarizarComissaoGeral(mesCompetencia);
        
        if (faturamento == null) faturamento = BigDecimal.ZERO;
        if (comissoes == null) comissoes = BigDecimal.ZERO;
        
        Double custo = 0.0;
        if (faturamento.compareTo(BigDecimal.ZERO) > 0) {
            custo = comissoes.divide(faturamento, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
        }
        
        return new ExecutivaKPIsDTO(faturamento, comissoes, custo);
    }

    public List<HistoricoEvolucaoDTO> getHistoricoExecutiva(LocalDate mesCompetencia) {
        LocalDate startDate = mesCompetencia.minusMonths(5).withDayOfMonth(1);
        LocalDate endDate = mesCompetencia.withDayOfMonth(mesCompetencia.lengthOfMonth());
        
        List<HistoricoEvolucaoDTO> faturamento = baseVendasRepository.historicoFaturamento(startDate, endDate);
        List<HistoricoEvolucaoDTO> comissoes = comissaoRepository.historicoComissoes(startDate, endDate);
        
        // Merge the two lists by dateRef
        for (HistoricoEvolucaoDTO fat : faturamento) {
            for (HistoricoEvolucaoDTO com : comissoes) {
                if (fat.getMes().equals(com.getMes())) {
                    fat.setComissoes(com.getComissoes());
                    break;
                }
            }
        }
        return faturamento;
    }

    public List<DashboardAgregacaoDTO> getTopMarcas(LocalDate mesCompetencia) {
        return baseVendasRepository.sumarizarFaturamentoPorMarca(mesCompetencia);
    }

    public List<RankingVendedorDTO> getRankingVendedores(LocalDate mesCompetencia) {
        List<RankingVendedorDTO> ranking = comissaoRepository.rankingVendedores(mesCompetencia);
        return ranking.subList(0, Math.min(10, ranking.size()));
    }

    public List<DashboardAgregacaoDTO> getProporcionalidadeRH(LocalDate mesCompetencia) {
        LocalDate startDate = mesCompetencia.withDayOfMonth(1);
        LocalDate endDate = mesCompetencia.withDayOfMonth(mesCompetencia.lengthOfMonth());
        return intercorrenciaRHRepository.sumarizarProporcionalidade(startDate, endDate);
    }

    public Long getAnomaliasCadastroRH(LocalDate mesCompetencia) {
        return baseRHRepository.contarAnomaliasSemLoja(mesCompetencia);
    }
}
