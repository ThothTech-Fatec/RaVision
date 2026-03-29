package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseComissionamento;
import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.BaseVendas;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.repository.BaseComissionamentoRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculoComissaoService {

    private final BaseRHRepository rhRepository;
    private final BaseVendasRepository vendasRepository;
    private final BaseComissionamentoRepository comissaoRepository;
    private final ComissaoCalculadaBaseRepository calculoRepository;

    @Transactional
    public void calcularCompetencia(LocalDate dateRef) {
        log.info("Iniciando calculo da competencia: {}", dateRef);

        // 1. Carregar Dados em Memoria (Batching)
        List<BaseRH> listaRH = rhRepository.findByDateRef(dateRef);
        List<BaseVendas> listaVendas = vendasRepository.findByDateRef(dateRef);
        List<BaseComissionamento> listaComissao = comissaoRepository.findAll();

        if (listaRH.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma base de RH encontrada para a competencia: " + dateRef);
        }

        // 2. Criar Mapas de Agrupamento O(N)
        // Vendas Consolidadas por Vendedor (Matricula)
        Map<String, BigDecimal> vendasPorMatricula = listaVendas.stream()
                .collect(Collectors.toMap(
                        BaseVendas::getMatricula,
                        BaseVendas::getVlrVenda,
                        BigDecimal::add
                ));

        // Vendas Consolidadas Totais por Loja (CodLoja)
        Map<Integer, BigDecimal> vendasTotaisPorLoja = listaVendas.stream()
                .collect(Collectors.toMap(
                        BaseVendas::getCodLoja,
                        BaseVendas::getVlrVenda,
                        BigDecimal::add
                ));

        // Mapeamento de % de Comissao por chave composta (CodMarca_CodCargo)
        Map<String, BigDecimal> comissaoPorMarcaCargo = listaComissao.stream()
                .collect(Collectors.toMap(
                        c -> c.getCodMarca() + "_" + c.getCodCargo(),
                        BaseComissionamento::getPercentualComissao,
                        (existing, replacement) -> existing
                ));

        // 3. Limpar calculo previo
        calculoRepository.deleteByDateRef(dateRef);

        List<ComissaoCalculadaBase> calculosGerados = new ArrayList<>();

        // 4. Laco Principal usando CPU pura para fugir do I/O
        for (BaseRH rh : listaRH) {
            ComissaoCalculadaBase calc = new ComissaoCalculadaBase();
            calc.setDateRef(dateRef);
            calc.setMatricula(rh.getMatricula());
            calc.setCodLoja(rh.getCodLoja());
            calc.setCodCargo(rh.getCodCargo());

            // Identificar o percentual
            String chaveComissao = rh.getCodMarca() + "_" + rh.getCodCargo();
            BigDecimal percentual = comissaoPorMarcaCargo.getOrDefault(chaveComissao, BigDecimal.ZERO);
            calc.setPercentualAplicado(percentual);

            BigDecimal valorBase = BigDecimal.ZERO;

            if (rh.getCodCargo() == 150) {
                // Regra Especial 4: Gerente toma como base a venda TOTAL da loja
                valorBase = vendasTotaisPorLoja.getOrDefault(rh.getCodLoja(), BigDecimal.ZERO);
            } else {
                // Regra Geral 3: Vendedor toma como base a venda INDIVIDUAL
                valorBase = vendasPorMatricula.getOrDefault(rh.getMatricula(), BigDecimal.ZERO);
            }

            calc.setValorBaseVendas(valorBase);
            
            // Calculo da comissao: Base * Percentual (%)
            // Transforma o percentual de formato 2.50 para multiplicador matematico (0.025)
            BigDecimal multiplicadorPercentual = percentual.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal valorComissao = valorBase.multiply(multiplicadorPercentual).setScale(2, RoundingMode.HALF_UP);
            
            calc.setValorComissaoGerado(valorComissao);

            calculosGerados.add(calc);
        }

        calculoRepository.saveAll(calculosGerados);
        log.info("Calculo finalizado para {} funcionarios na competencia {}", calculosGerados.size(), dateRef);
    }

    public List<ComissaoCalculadaBase> buscarPorCompetencia(LocalDate dateRef) {
        return calculoRepository.findByDateRef(dateRef);
    }
}
