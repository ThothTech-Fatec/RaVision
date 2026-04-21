package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.domain.ComissaoCalculadaProporcional;
import br.com.ravision.backend.domain.IntercorrenciaRH;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaProporcionalRepository;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculoProporcionalService {

    private final ComissaoCalculadaBaseRepository comissaoBaseRepository;
    private final BaseRHRepository rhRepository;
    private final IntercorrenciaRHRepository intercorrenciaRepository;
    private final ComissaoCalculadaProporcionalRepository proporcionalRepository;

    private static final BigDecimal PISO_SALARIAL = new BigDecimal("3500.00");

    @Transactional
    public void calcularProporcional(LocalDate dateRef) {
        log.info("Iniciando Calculo Proporcional para competencia: {}", dateRef);

        List<ComissaoCalculadaBase> comissoesBase = comissaoBaseRepository.findByDateRef(dateRef);
        List<BaseRH> listaRH = rhRepository.findByDateRef(dateRef);
        List<IntercorrenciaRH> intercorrencias = intercorrenciaRepository.findAll();

        if (comissoesBase.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma comissao base encontrada para a dateRef. Rode a Etapa 02 primeiro.");
        }

        Map<String, BaseRH> rhMap = listaRH.stream()
                .collect(Collectors.toMap(BaseRH::getMatricula, r -> r, (r1, r2) -> r1));

        Map<String, List<IntercorrenciaRH>> interMap = intercorrencias.stream()
                .collect(Collectors.groupingBy(IntercorrenciaRH::getMatricula));

        YearMonth yearMonth = YearMonth.from(dateRef);
        int diasDoMes = yearMonth.lengthOfMonth();

        proporcionalRepository.deleteByDateRef(dateRef);
        List<ComissaoCalculadaProporcional> calculosFinais = new ArrayList<>();

        for (ComissaoCalculadaBase base : comissoesBase) {
            String matricula = base.getMatricula();
            BaseRH rh = rhMap.get(matricula);

            if (rh == null) {
                log.warn("Nao encontrado RH para matricula {}", matricula);
                continue;
            }

            List<IntercorrenciaRH> intsFunc = interMap.getOrDefault(matricula, new ArrayList<>());

            int diasTrabalhados = diasDoMes;
            String motivo = "INTEGRAL";

            boolean licencaMaternidade = intsFunc.stream().anyMatch(i -> "LICENCA_MATERNIDADE".equalsIgnoreCase(i.getTipo()));

            if (licencaMaternidade) {
                motivo = "LICENCA_MATERNIDADE";
                diasTrabalhados = diasDoMes; // Isencao total de perdas
            } else if (rh.getDataAdmissao() != null && YearMonth.from(rh.getDataAdmissao()).equals(yearMonth)) {
                motivo = "ADMISSAO";
                diasTrabalhados = diasDoMes - rh.getDataAdmissao().getDayOfMonth() + 1;
            } else if (rh.getDataDemissao() != null && YearMonth.from(rh.getDataDemissao()).equals(yearMonth)) {
                motivo = "DEMISSAO";
                diasTrabalhados = rh.getDataDemissao().getDayOfMonth();
            } else {
                int diasFerias = calcularDiasIntercorrenciaNoMes(intsFunc, yearMonth, "FERIAS");
                int diasAfastamento = calcularDiasIntercorrenciaNoMes(intsFunc, yearMonth, "ATESTADO", "AFASTAMENTO");

                if (diasFerias > 0) {
                    motivo = "FERIAS";
                    diasTrabalhados -= diasFerias;
                }
                if (diasAfastamento > 0) {
                    motivo = "AFASTAMENTO";
                    diasTrabalhados -= diasAfastamento;
                }
            }

            if (diasTrabalhados < 0) diasTrabalhados = 0;

            // Multiplas Lojas (Transferencia)
            IntercorrenciaRH transferencia = intsFunc.stream()
                    .filter(i -> "TRANSFERENCIA".equalsIgnoreCase(i.getTipo()))
                    .findFirst().orElse(null);

            if (transferencia != null && transferencia.getCodLojaSecundaria() != null) {
                int diasLojaSecundaria = calcularDiasIntercorrenciaNoMes(List.of(transferencia), yearMonth, "TRANSFERENCIA");
                int diasLojaPrincipal = diasTrabalhados - diasLojaSecundaria;

                if (diasLojaSecundaria > 0 && diasLojaPrincipal > 0) {
                    // Loja Principal
                    ComissaoCalculadaProporcional prop1 = buildProporcional(base, dateRef, diasDoMes, diasLojaPrincipal, "RATEIO_LOJA_PRINCIPAL");
                    prop1.setCodLoja(base.getCodLoja());
                    prop1.setValorComissaoProporcional(calcularValorComissao(base, diasDoMes, diasLojaPrincipal));
                    calculosFinais.add(prop1);

                    // Loja Secundaria
                    ComissaoCalculadaProporcional prop2 = buildProporcional(base, dateRef, diasDoMes, diasLojaSecundaria, "RATEIO_LOJA_SECUNDARIA");
                    prop2.setCodLoja(transferencia.getCodLojaSecundaria());
                    prop2.setValorComissaoProporcional(calcularValorComissao(base, diasDoMes, diasLojaSecundaria));
                    calculosFinais.add(prop2);
                    continue;
                }
            }

            // Calculo Normal / Trabalhista
            ComissaoCalculadaProporcional prop = buildProporcional(base, dateRef, diasDoMes, diasTrabalhados, motivo);
            prop.setCodLoja(base.getCodLoja());

            BigDecimal finalValue;

            if (licencaMaternidade) {
                finalValue = base.getValorComissaoGerado();
            } else if ("AFASTAMENTO".equals(motivo)) {
                int diasAfast = calcularDiasIntercorrenciaNoMes(intsFunc, yearMonth, "ATESTADO", "AFASTAMENTO");
                int diasPagosEmpresa = Math.min(diasAfast, 15);

                BigDecimal vendasDoMes = base.getValorBaseVendas();
                BigDecimal mediaDiariaVendas = diasTrabalhados > 0 
                        ? vendasDoMes.divide(BigDecimal.valueOf(diasTrabalhados), 8, RoundingMode.HALF_UP) 
                        : BigDecimal.ZERO;

                BigDecimal baseAdicional = mediaDiariaVendas.multiply(BigDecimal.valueOf(diasPagosEmpresa));
                BigDecimal comissaoAdicional = baseAdicional.multiply(base.getPercentualAplicado());

                // Comissao total = comissao real + comissao adicional
                finalValue = base.getValorComissaoGerado().add(comissaoAdicional).setScale(2, RoundingMode.HALF_UP);

                // Compara com o piso de 3500
                if (finalValue.compareTo(PISO_SALARIAL) < 0) {
                    finalValue = PISO_SALARIAL;
                }
            } else {
                finalValue = calcularValorComissao(base, diasDoMes, diasTrabalhados);
            }

            prop.setValorComissaoProporcional(finalValue);
            calculosFinais.add(prop);
        }

        proporcionalRepository.saveAll(calculosFinais);
        log.info("Calculo Proporcional finalizado com sucesso.");
    }

    private ComissaoCalculadaProporcional buildProporcional(ComissaoCalculadaBase base, LocalDate dateRef, int diasDoMes, int diasTrabalhados, String motivo) {
        ComissaoCalculadaProporcional prop = new ComissaoCalculadaProporcional();
        prop.setIdComissaoBase(base.getId());
        prop.setDateRef(dateRef);
        prop.setMatricula(base.getMatricula());
        prop.setDiasDoMes(diasDoMes);
        prop.setDiasTrabalhados(diasTrabalhados);
        prop.setMotivoProporcionalidade(motivo);
        return prop;
    }

    private BigDecimal calcularValorComissao(ComissaoCalculadaBase base, int diasDoMes, int diasTrabalhados) {
        if (diasTrabalhados == diasDoMes) return base.getValorComissaoGerado();
        if (diasTrabalhados <= 0) return BigDecimal.ZERO;
        
        BigDecimal porDia = base.getValorComissaoGerado().divide(BigDecimal.valueOf(diasDoMes), 8, RoundingMode.HALF_UP);
        return porDia.multiply(BigDecimal.valueOf(diasTrabalhados)).setScale(2, RoundingMode.HALF_UP);
    }

    private int calcularDiasIntercorrenciaNoMes(List<IntercorrenciaRH> list, YearMonth compet, String... tipos) {
        int dias = 0;
        LocalDate inicioMes = compet.atDay(1);
        LocalDate fimMes = compet.atEndOfMonth();
        List<String> tiposList = Arrays.asList(tipos);

        for (IntercorrenciaRH f : list) {
            if (!tiposList.contains(f.getTipo().toUpperCase())) continue;

            LocalDate inicio = f.getDataInicio();
            LocalDate fim = f.getDataFim();

            if (inicio.isAfter(fimMes) || fim.isBefore(inicioMes)) continue;

            LocalDate startCalc = inicio.isBefore(inicioMes) ? inicioMes : inicio;
            LocalDate endCalc = fim.isAfter(fimMes) ? fimMes : fim;

            long overlap = java.time.temporal.ChronoUnit.DAYS.between(startCalc, endCalc) + 1;
            dias += (int) overlap;
        }
        return dias;
    }
}
