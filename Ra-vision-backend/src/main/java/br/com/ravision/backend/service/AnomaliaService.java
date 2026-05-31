package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.dto.AnomaliaDTO;
import br.com.ravision.backend.dto.AuditarAnomaliaDTO;
import br.com.ravision.backend.dto.ConfiguracaoAnomaliaDTO;
import br.com.ravision.backend.repository.AnomaliaRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaFinalRepository;
import br.com.ravision.backend.repository.ConfiguracaoAnomaliaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomaliaService {

    private final AnomaliaRepository anomaliaRepository;
    private final ConfiguracaoAnomaliaRepository configuracaoRepository;
    private final ComissaoCalculadaFinalRepository comissaoFinalRepository;
    private final BaseRHRepository rhRepository;

    @Transactional
    public ConfiguracaoAnomalia salvarConfiguracao(ConfiguracaoAnomaliaDTO dto) {
        ConfiguracaoAnomalia config = ConfiguracaoAnomalia.builder()
                .algoritmo(dto.getAlgoritmo() != null ? dto.getAlgoritmo() : TipoAlgoritmo.IQR)
                .limiteMultiplicador(dto.getLimiteMultiplicador() != null ? dto.getLimiteMultiplicador() : 1.5)
                .agruparPorCargo(dto.getAgruparPorCargo() != null ? dto.getAgruparPorCargo() : true)
                .build();
        return configuracaoRepository.save(config);
    }

    public ConfiguracaoAnomalia obterConfiguracaoAtiva() {
        return configuracaoRepository.findTopByOrderByAtualizadoEmDesc()
                .orElseGet(() -> ConfiguracaoAnomalia.builder()
                        .algoritmo(TipoAlgoritmo.IQR)
                        .limiteMultiplicador(1.5)
                        .agruparPorCargo(true)
                        .build());
    }

    @Transactional
    public Map<String, Object> dispararDeteccaoAnomalias(LocalDate mesCompetencia) {
        log.info("Iniciando detecção de anomalias para a competência: {}", mesCompetencia);

        ConfiguracaoAnomalia config = obterConfiguracaoAtiva();

        // 1. Limpar anomalias pendentes para reprocessamento
        anomaliaRepository.deletePendenteByDateRef(mesCompetencia);

        // 2. Coletar dados
        List<ComissaoCalculadaFinal> comissoes = comissaoFinalRepository.findByDateRef(mesCompetencia);
        List<BaseRH> rhList = rhRepository.findByDateRef(mesCompetencia);

        if (comissoes.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma comissão encontrada para a competência informada.");
        }

        // Mapear matricula -> codCargo
        Map<String, Integer> mapCargoPorMatricula = rhList.stream()
                .collect(Collectors.toMap(BaseRH::getMatricula, BaseRH::getCodCargo, (c1, c2) -> c1));

        // 3. Agrupamento (se configurado para agrupar por cargo)
        Map<Integer, List<ComissaoCalculadaFinal>> comissoesAgrupadas = new HashMap<>();
        if (config.getAgruparPorCargo()) {
            comissoesAgrupadas = comissoes.stream()
                    .collect(Collectors.groupingBy(c -> mapCargoPorMatricula.getOrDefault(c.getMatricula(), 0)));
        } else {
            comissoesAgrupadas.put(0, comissoes); // Grupo único
        }

        List<Anomalia> anomaliasDetectadas = new ArrayList<>();

        // 4. Processar cada grupo
        for (Map.Entry<Integer, List<ComissaoCalculadaFinal>> entry : comissoesAgrupadas.entrySet()) {
            Integer codCargo = entry.getKey();
            List<ComissaoCalculadaFinal> listaGrupo = entry.getValue();

            if (listaGrupo.size() < 3) {
                log.warn("Grupo com poucos dados (Cargo {}). Pulando análise estatística.", codCargo);
                continue;
            }

            if (config.getAlgoritmo() == TipoAlgoritmo.IQR) {
                anomaliasDetectadas.addAll(calcularPorIQR(listaGrupo, codCargo, mesCompetencia, config.getLimiteMultiplicador()));
            } else {
                anomaliasDetectadas.addAll(calcularPorZScore(listaGrupo, codCargo, mesCompetencia, config.getLimiteMultiplicador()));
            }
        }

        // 5. Persistir anomalias
        anomaliaRepository.saveAll(anomaliasDetectadas);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "Concluído");
        resultado.put("totalAnalisados", comissoes.size());
        resultado.put("anomaliasDetectadas", anomaliasDetectadas.size());
        return resultado;
    }

    private List<Anomalia> calcularPorIQR(List<ComissaoCalculadaFinal> comissoes, Integer codCargo, LocalDate dateRef, Double k) {
        List<Anomalia> anomalias = new ArrayList<>();

        // Ordenar valores
        List<BigDecimal> valores = comissoes.stream()
                .map(ComissaoCalculadaFinal::getValorComissaoFinal)
                .sorted()
                .collect(Collectors.toList());

        int n = valores.size();
        BigDecimal q1 = valores.get(n / 4);
        BigDecimal q3 = valores.get(n * 3 / 4);
        BigDecimal iqr = q3.subtract(q1);

        BigDecimal limiteInferior = q1.subtract(iqr.multiply(BigDecimal.valueOf(k)));
        BigDecimal limiteSuperior = q3.add(iqr.multiply(BigDecimal.valueOf(k)));

        for (ComissaoCalculadaFinal c : comissoes) {
            BigDecimal valor = c.getValorComissaoFinal();
            if (valor.compareTo(limiteSuperior) > 0) {
                anomalias.add(buildAnomalia(c, codCargo, dateRef, valor, limiteSuperior, null, TipoAnomalia.MUITO_ACIMA_DA_MEDIA));
            } else if (valor.compareTo(limiteInferior) < 0) {
                anomalias.add(buildAnomalia(c, codCargo, dateRef, valor, limiteInferior, null, TipoAnomalia.MUITO_ABAIXO_DA_MEDIA));
            }
        }
        return anomalias;
    }

    private List<Anomalia> calcularPorZScore(List<ComissaoCalculadaFinal> comissoes, Integer codCargo, LocalDate dateRef, Double k) {
        List<Anomalia> anomalias = new ArrayList<>();

        int n = comissoes.size();
        BigDecimal soma = comissoes.stream()
                .map(ComissaoCalculadaFinal::getValorComissaoFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal media = soma.divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP);

        BigDecimal somaDiferencasQuadradas = comissoes.stream()
                .map(c -> {
                    BigDecimal diff = c.getValorComissaoFinal().subtract(media);
                    return diff.multiply(diff);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double variancia = somaDiferencasQuadradas.doubleValue() / n;
        double desvioPadrao = Math.sqrt(variancia);

        BigDecimal limiteSuperior = media.add(BigDecimal.valueOf(desvioPadrao * k));
        BigDecimal limiteInferior = media.subtract(BigDecimal.valueOf(desvioPadrao * k));

        for (ComissaoCalculadaFinal c : comissoes) {
            BigDecimal valor = c.getValorComissaoFinal();
            if (valor.compareTo(limiteSuperior) > 0) {
                anomalias.add(buildAnomalia(c, codCargo, dateRef, valor, limiteSuperior, media, TipoAnomalia.MUITO_ACIMA_DA_MEDIA));
            } else if (valor.compareTo(limiteInferior) < 0) {
                anomalias.add(buildAnomalia(c, codCargo, dateRef, valor, limiteInferior, media, TipoAnomalia.MUITO_ABAIXO_DA_MEDIA));
            }
        }
        return anomalias;
    }

    private Anomalia buildAnomalia(ComissaoCalculadaFinal c, Integer codCargo, LocalDate dateRef, BigDecimal valor, BigDecimal limite, BigDecimal media, TipoAnomalia tipo) {
        return Anomalia.builder()
                .dateRef(dateRef)
                .matricula(c.getMatricula())
                .codCargo(codCargo)
                .valorComissao(valor)
                .limiteEsperado(limite.setScale(2, RoundingMode.HALF_UP))
                .valorMedio(media != null ? media.setScale(2, RoundingMode.HALF_UP) : null)
                .tipoAnomalia(tipo)
                .statusAnalise(StatusAnaliseAnomalia.PENDENTE)
                .build();
    }

    public Page<AnomaliaDTO> buscarAnomalias(LocalDate dateRef, String matricula, TipoAnomalia tipoAnomalia, StatusAnaliseAnomalia statusAnalise, Pageable pageable) {
        return anomaliaRepository.findWithFilters(dateRef, matricula, tipoAnomalia, statusAnalise, pageable)
                .map(a -> AnomaliaDTO.builder()
                        .id(a.getId())
                        .dateRef(a.getDateRef())
                        .matricula(a.getMatricula())
                        .codCargo(a.getCodCargo())
                        .valorComissao(a.getValorComissao())
                        .limiteEsperado(a.getLimiteEsperado())
                        .valorMedio(a.getValorMedio())
                        .tipoAnomalia(a.getTipoAnomalia())
                        .statusAnalise(a.getStatusAnalise())
                        .justificativa(a.getJustificativa())
                        .build());
    }

    @Transactional
    public void auditarAnomalia(Long id, AuditarAnomaliaDTO dto) {
        Anomalia anomalia = anomaliaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anomalia não encontrada"));
        
        anomalia.setStatusAnalise(dto.getStatusAnalise());
        anomalia.setJustificativa(dto.getJustificativa());
        anomaliaRepository.save(anomalia);
    }
}
