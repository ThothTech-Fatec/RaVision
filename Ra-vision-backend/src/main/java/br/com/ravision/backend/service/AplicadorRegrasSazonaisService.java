package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AplicadorRegrasSazonaisService {

    private final RegraNegocioDinamicaRepository regraRepository;
    private final ComissaoCalculadaProporcionalRepository proporcionalRepository;
    private final ComissaoCalculadaBaseRepository baseRepository;
    private final BaseRHRepository rhRepository;
    private final ComissaoCalculadaFinalRepository finalRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void aplicarRegrasSazonais(LocalDate dateRef) {
        log.info("Aplicando regras sazonais para competencia: {}", dateRef);

        String mesCompetencia = dateRef.getYear() + "-" + String.format("%02d", dateRef.getMonthValue());
        List<RegraNegocioDinamica> regrasAprovadas = regraRepository.findByMesCompetenciaAndStatusAprovacao(mesCompetencia, StatusAprovacao.APROVADA);

        if (regrasAprovadas.isEmpty()) {
            log.info("Nenhuma regra aprovada encontrada para {}", mesCompetencia);
        }

        List<ComissaoCalculadaProporcional> proporcionais = proporcionalRepository.findByDateRef(dateRef);
        if (proporcionais.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma comissao proporcional encontrada para a dateRef.");
        }

        Map<Long, ComissaoCalculadaBase> baseMap = baseRepository.findByDateRef(dateRef).stream()
                .collect(Collectors.toMap(ComissaoCalculadaBase::getId, b -> b));
        
        Map<String, BaseRH> rhMap = rhRepository.findByDateRef(dateRef).stream()
                .collect(Collectors.toMap(BaseRH::getMatricula, r -> r, (r1, r2) -> r1));

        finalRepository.deleteByDateRef(dateRef);
        List<ComissaoCalculadaFinal> comissoesFinais = new ArrayList<>();

        for (ComissaoCalculadaProporcional prop : proporcionais) {
            ComissaoCalculadaBase base = baseMap.get(prop.getIdComissaoBase());
            BaseRH rh = rhMap.get(prop.getMatricula());

            if (base == null || rh == null) continue;

            BigDecimal valorFinal = prop.getValorComissaoProporcional();
            List<String> regrasAplicadas = new ArrayList<>();

            // Separar regras Faixa de Vendas para aplicar apenas a maior
            List<RegraNegocioDinamica> regrasFaixaVendasValidas = new ArrayList<>();

            for (RegraNegocioDinamica regra : regrasAprovadas) {
                if (!seAplica(regra.getCondicoesAplicacao(), rh, base)) continue;

                if (regra.getTipoRegra() == TipoRegra.FAIXA_VENDAS) {
                    regrasFaixaVendasValidas.add(regra);
                    continue; // processa depois
                }

                valorFinal = processarRegra(regra, valorFinal, base, rh, prop, regrasAplicadas);
            }

            // Aplicar a maior Faixa de Vendas
            if (!regrasFaixaVendasValidas.isEmpty()) {
                RegraNegocioDinamica maiorRegra = regrasFaixaVendasValidas.stream()
                        .max(Comparator.comparing(RegraNegocioDinamica::getValorModificador))
                        .orElse(null);
                if (maiorRegra != null) {
                    valorFinal = processarRegra(maiorRegra, valorFinal, base, rh, prop, regrasAplicadas);
                }
            }

            ComissaoCalculadaFinal ccf = new ComissaoCalculadaFinal();
            ccf.setIdComissaoProporcional(prop.getId());
            ccf.setDateRef(dateRef);
            ccf.setMatricula(prop.getMatricula());
            ccf.setValorComissaoFinal(valorFinal.setScale(2, RoundingMode.HALF_UP));
            ccf.setHistoricoRegrasAplicadas(String.join(", ", regrasAplicadas));

            comissoesFinais.add(ccf);
        }

        finalRepository.saveAll(comissoesFinais);
        log.info("Regras sazonais aplicadas. {} comissoes finais geradas.", comissoesFinais.size());
    }

    private BigDecimal processarRegra(RegraNegocioDinamica regra, BigDecimal valorFinalAtual, ComissaoCalculadaBase base, BaseRH rh, ComissaoCalculadaProporcional prop, List<String> regrasAplicadas) {
        BigDecimal novoValor = valorFinalAtual;
        
        try {
            switch (regra.getTipoRegra()) {
                case BONUS_FIXO:
                    novoValor = novoValor.add(regra.getValorModificador());
                    regrasAplicadas.add("BONUS_FIXO(+" + regra.getValorModificador() + ")");
                    break;
                
                case BONUS_BASE:
                    BigDecimal fracaoTrabalhada = BigDecimal.valueOf(prop.getDiasTrabalhados()).divide(BigDecimal.valueOf(prop.getDiasDoMes()), 8, RoundingMode.HALF_UP);
                    BigDecimal bonusNaBase = regra.getValorModificador().multiply(base.getPercentualAplicado()).multiply(fracaoTrabalhada);
                    novoValor = novoValor.add(bonusNaBase);
                    regrasAplicadas.add("BONUS_BASE(+" + bonusNaBase.setScale(2, RoundingMode.HALF_UP) + ")");
                    break;

                case OVERRIDE_PERCENTUAL:
                    BigDecimal fracao = BigDecimal.valueOf(prop.getDiasTrabalhados()).divide(BigDecimal.valueOf(prop.getDiasDoMes()), 8, RoundingMode.HALF_UP);
                    BigDecimal percentualOriginal = base.getPercentualAplicado();
                    BigDecimal comissaoAntiga = base.getValorBaseVendas().multiply(percentualOriginal).multiply(fracao);
                    BigDecimal comissaoNova = base.getValorBaseVendas().multiply(regra.getValorModificador()).multiply(fracao);
                    novoValor = novoValor.subtract(comissaoAntiga).add(comissaoNova);
                    regrasAplicadas.add("OVERRIDE_PERCENTUAL(" + regra.getValorModificador() + ")");
                    break;

                case BLACK_FRIDAY:
                    BigDecimal fatiaBF = base.getValorBaseVendas().divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(7));
                    BigDecimal acrescimo = (rh.getCodCargo() != null && rh.getCodCargo() == 150) ? new BigDecimal("0.005") : new BigDecimal("0.01");
                    BigDecimal bonusBF = fatiaBF.multiply(acrescimo);
                    novoValor = novoValor.add(bonusBF);
                    regrasAplicadas.add("BLACK_FRIDAY(+" + bonusBF.setScale(2, RoundingMode.HALF_UP) + ")");
                    break;

                case FAIXA_VENDAS:
                    novoValor = novoValor.add(regra.getValorModificador());
                    regrasAplicadas.add("FAIXA_VENDAS(+" + regra.getValorModificador() + ")");
                    break;
            }
        } catch (Exception e) {
            log.error("Erro ao aplicar regra {}", regra.getId(), e);
        }
        
        return novoValor;
    }

    private boolean seAplica(String jsonCondicoes, BaseRH rh, ComissaoCalculadaBase base) {
        if (jsonCondicoes == null || jsonCondicoes.trim().isEmpty()) return true;

        try {
            Map<String, Object> condicoes = objectMapper.readValue(jsonCondicoes, new TypeReference<Map<String, Object>>() {});
            
            if (condicoes.containsKey("matricula")) {
                if (!String.valueOf(condicoes.get("matricula")).equals(rh.getMatricula())) return false;
            }
            if (condicoes.containsKey("codCargo")) {
                if (!String.valueOf(condicoes.get("codCargo")).equals(String.valueOf(rh.getCodCargo()))) return false;
            }
            if (condicoes.containsKey("codMarca")) {
                if (!String.valueOf(condicoes.get("codMarca")).equals(String.valueOf(rh.getCodMarca()))) return false;
            }
            if (condicoes.containsKey("codLoja")) {
                if (!String.valueOf(condicoes.get("codLoja")).equals(String.valueOf(base.getCodLoja()))) return false;
            }
            if (condicoes.containsKey("minVendas")) {
                BigDecimal minVendas = new BigDecimal(String.valueOf(condicoes.get("minVendas")));
                if (base.getValorBaseVendas().compareTo(minVendas) < 0) return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Erro ao fazer parse do JSON de condicoes", e);
            return false;
        }
    }
}
