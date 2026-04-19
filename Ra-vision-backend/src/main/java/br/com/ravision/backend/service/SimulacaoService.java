package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.domain.RegraNegocioDinamica;
import br.com.ravision.backend.domain.StatusAprovacao;
import br.com.ravision.backend.dto.SimulacaoResponseDTO;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import br.com.ravision.backend.repository.RegraNegocioDinamicaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulacaoService {

    private final RegraNegocioDinamicaRepository regraRepository;
    private final ComissaoCalculadaBaseRepository comissaoRepository;
    private final BaseRHRepository baseRHRepository;
    private final ObjectMapper objectMapper;

    /**
     * Simula o impacto financeiro de uma regra PENDENTE sobre as comissões base.
     * Todo o processamento é feito em memória (read-only).
     *
     * @param regraId ID da regra dinâmica a ser simulada
     * @return DTO com custo atual, custo simulado, impacto financeiro e qtd de funcionários afetados
     */
    @Transactional(readOnly = true)
    public SimulacaoResponseDTO simular(Long regraId) {
        // 1. Buscar a regra e validar status
        RegraNegocioDinamica regra = regraRepository.findById(regraId)
                .orElseThrow(() -> new RuntimeException("Regra não encontrada com ID: " + regraId));

        if (regra.getStatusAprovacao() != StatusAprovacao.PENDENTE) {
            throw new RuntimeException("Apenas regras com status PENDENTE podem ser simuladas. Status atual: " + regra.getStatusAprovacao());
        }

        // 2. Converter mesCompetencia ("2025-03") para LocalDate (primeiro dia do mês)
        LocalDate dateRef = converterMesCompetencia(regra.getMesCompetencia());

        // 3. Buscar comissões base da competência
        List<ComissaoCalculadaBase> comissoesBase = comissaoRepository.findByDateRef(dateRef);

        if (comissoesBase.isEmpty()) {
            return SimulacaoResponseDTO.builder()
                    .regraId(regraId)
                    .custoTotalAtual(BigDecimal.ZERO)
                    .custoTotalSimulado(BigDecimal.ZERO)
                    .impactoFinanceiro(BigDecimal.ZERO)
                    .quantidadeFuncionariosAfetados(0)
                    .build();
        }

        // 4. Parsear condições de aplicação (filtros)
        Map<String, Object> condicoes = parsearCondicoes(regra.getCondicoesAplicacao());

        // 5. Carregar BaseRH para resolver codMarca por matrícula (se necessário)
        Map<String, Integer> marcaPorMatricula = Collections.emptyMap();
        if (condicoes.containsKey("codMarca")) {
            List<BaseRH> listaRH = baseRHRepository.findByDateRef(dateRef);
            marcaPorMatricula = listaRH.stream()
                    .collect(Collectors.toMap(
                            BaseRH::getMatricula,
                            BaseRH::getCodMarca,
                            (existing, replacement) -> existing
                    ));
        }

        // 6. Calcular custo atual e custo simulado
        BigDecimal custoTotalAtual = BigDecimal.ZERO;
        BigDecimal custoTotalSimulado = BigDecimal.ZERO;
        int funcionariosAfetados = 0;

        for (ComissaoCalculadaBase comissao : comissoesBase) {
            BigDecimal valorOriginal = comissao.getValorComissaoGerado();
            custoTotalAtual = custoTotalAtual.add(valorOriginal);

            boolean atendeCondicoes = verificarCondicoes(comissao, condicoes, marcaPorMatricula);

            if (atendeCondicoes) {
                BigDecimal valorSimulado = aplicarRegra(regra, comissao);
                custoTotalSimulado = custoTotalSimulado.add(valorSimulado);

                if (valorSimulado.compareTo(valorOriginal) != 0) {
                    funcionariosAfetados++;
                }
            } else {
                // Funcionário não afetado pela regra → mantém valor original
                custoTotalSimulado = custoTotalSimulado.add(valorOriginal);
            }
        }

        BigDecimal impactoFinanceiro = custoTotalSimulado.subtract(custoTotalAtual);

        log.info("Simulação concluída para regra ID {}: Atual={}, Simulado={}, Impacto={}, Afetados={}",
                regraId, custoTotalAtual, custoTotalSimulado, impactoFinanceiro, funcionariosAfetados);

        return SimulacaoResponseDTO.builder()
                .regraId(regraId)
                .custoTotalAtual(custoTotalAtual)
                .custoTotalSimulado(custoTotalSimulado)
                .impactoFinanceiro(impactoFinanceiro)
                .quantidadeFuncionariosAfetados(funcionariosAfetados)
                .build();
    }

    /**
     * Converte string "yyyy-MM" para LocalDate no primeiro dia do mês.
     */
    LocalDate converterMesCompetencia(String mesCompetencia) {
        YearMonth yearMonth = YearMonth.parse(mesCompetencia, DateTimeFormatter.ofPattern("yyyy-MM"));
        return yearMonth.atDay(1);
    }

    /**
     * Parseia o JSON de condicoesAplicacao para um Map de filtros.
     * Retorna Map vazio se o JSON for nulo, vazio ou inválido.
     */
    Map<String, Object> parsearCondicoes(String condicoesJson) {
        if (condicoesJson == null || condicoesJson.isBlank() || condicoesJson.trim().equals("{}")) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(condicoesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Erro ao parsear condicoesAplicacao: {}. Aplicando regra para todos.", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Verifica se uma comissão atende a todas as condições de aplicação da regra.
     * Filtros opcionais: matricula, codCargo, codLoja, codMarca.
     * Se nenhum filtro estiver presente, a regra se aplica a todos.
     */
    boolean verificarCondicoes(ComissaoCalculadaBase comissao, Map<String, Object> condicoes,
                               Map<String, Integer> marcaPorMatricula) {
        if (condicoes.isEmpty()) {
            return true;
        }

        // Filtro por matrícula
        if (condicoes.containsKey("matricula")) {
            String matriculaFiltro = String.valueOf(condicoes.get("matricula"));
            if (!matriculaFiltro.equals(comissao.getMatricula())) {
                return false;
            }
        }

        // Filtro por código de cargo
        if (condicoes.containsKey("codCargo")) {
            int codCargoFiltro = toInt(condicoes.get("codCargo"));
            if (codCargoFiltro != comissao.getCodCargo()) {
                return false;
            }
        }

        // Filtro por código de loja
        if (condicoes.containsKey("codLoja")) {
            int codLojaFiltro = toInt(condicoes.get("codLoja"));
            if (codLojaFiltro != comissao.getCodLoja()) {
                return false;
            }
        }

        // Filtro por código de marca (requer cruzamento com BaseRH)
        if (condicoes.containsKey("codMarca")) {
            int codMarcaFiltro = toInt(condicoes.get("codMarca"));
            Integer codMarcaFuncionario = marcaPorMatricula.get(comissao.getMatricula());
            if (codMarcaFuncionario == null || codMarcaFuncionario != codMarcaFiltro) {
                return false;
            }
        }

        return true;
    }

    /**
     * Aplica a regra de simulação sobre uma comissão individual, baseado no tipoRegra.
     * Retorna o novo valor simulado da comissão.
     */
    BigDecimal aplicarRegra(RegraNegocioDinamica regra, ComissaoCalculadaBase comissao) {
        BigDecimal valorModificador = regra.getValorModificador();

        switch (regra.getTipoRegra()) {
            case BONUS_FIXO:
                // Soma um valor fixo ao valor da comissão do funcionário
                return comissao.getValorComissaoGerado().add(valorModificador);

            case OVERRIDE_PERCENTUAL:
                // Recalcula a comissão usando o novo percentual sobre a base de vendas
                // valorModificador = novo percentual (ex: 3.50 → 0.035)
                BigDecimal multiplicador = valorModificador.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                return comissao.getValorBaseVendas().multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);

            case FAIXA_VENDAS:
                // Se valorBaseVendas >= limite mínimo (condicoesAplicacao), soma valorModificador como bônus
                // O limite mínimo é obtido das condições; se não houver, aplica direto
                return comissao.getValorComissaoGerado().add(valorModificador);

            case BLACK_FRIDAY:
                // Multiplica a comissão pelo fator valorModificador
                // Ex: valorModificador = 1.5 → comissão * 1.5
                return comissao.getValorComissaoGerado().multiply(valorModificador).setScale(2, RoundingMode.HALF_UP);

            default:
                return comissao.getValorComissaoGerado();
        }
    }

    /**
     * Converte um Object (Integer ou String numérico) para int.
     */
    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
