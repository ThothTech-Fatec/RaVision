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

        Map<String, List<IntercorrenciaRH>> feriasMap = intercorrencias.stream()
                .filter(i -> "FERIAS".equalsIgnoreCase(i.getTipo()))
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

            ComissaoCalculadaProporcional prop = new ComissaoCalculadaProporcional();
            prop.setIdComissaoBase(base.getId());
            prop.setDateRef(dateRef);
            prop.setMatricula(matricula);
            prop.setDiasDoMes(diasDoMes);

            int diasTrabalhados;
            String motivo;

            // Logica Exata Conforme Prd do Usuario
            if (rh.getDataAdmissao() != null && YearMonth.from(rh.getDataAdmissao()).equals(yearMonth)) {
                motivo = "ADMISSAO";
                diasTrabalhados = diasDoMes - rh.getDataAdmissao().getDayOfMonth();
            } else if (rh.getDataDemissao() != null && YearMonth.from(rh.getDataDemissao()).equals(yearMonth)) {
                motivo = "DEMISSAO";
                diasTrabalhados = rh.getDataDemissao().getDayOfMonth();
            } else {
                List<IntercorrenciaRH> feriasList = feriasMap.getOrDefault(matricula, new ArrayList<>());
                int diasFeriasNoMes = calcularDiasFeriasNoMes(feriasList, yearMonth);

                if (diasFeriasNoMes > 0) {
                    motivo = "FERIAS";
                    diasTrabalhados = diasDoMes - diasFeriasNoMes;
                    if (diasTrabalhados < 0) diasTrabalhados = 0;
                } else {
                    motivo = "INTEGRAL";
                    diasTrabalhados = diasDoMes;
                }
            }

            prop.setDiasTrabalhados(diasTrabalhados);
            prop.setMotivoProporcionalidade(motivo);

            // Regra Trabalhista: (ValorBase Original / Quantidade De Dias do Mes Real) * Trabalhados
            BigDecimal valorBase = base.getValorComissaoGerado();
            
            if (diasTrabalhados == diasDoMes) {
                prop.setValorComissaoProporcional(valorBase);
            } else if (diasTrabalhados <= 0) {
                prop.setValorComissaoProporcional(BigDecimal.ZERO);
            } else {
                BigDecimal porDia = valorBase.divide(BigDecimal.valueOf(diasDoMes), 8, RoundingMode.HALF_UP);
                BigDecimal finalValue = porDia.multiply(BigDecimal.valueOf(diasTrabalhados)).setScale(2, RoundingMode.HALF_UP);
                prop.setValorComissaoProporcional(finalValue);
            }

            calculosFinais.add(prop);
        }

        proporcionalRepository.saveAll(calculosFinais);
        log.info("Calculo Proporcional finalizado com sucesso.");
    }

    private int calcularDiasFeriasNoMes(List<IntercorrenciaRH> feriasList, YearMonth compet) {
        int diasFerias = 0;
        LocalDate inicioMes = compet.atDay(1);
        LocalDate fimMes = compet.atEndOfMonth();

        for (IntercorrenciaRH f : feriasList) {
            LocalDate inicioFerias = f.getDataInicio();
            LocalDate fimFerias = f.getDataFim();

            if (inicioFerias.isAfter(fimMes) || fimFerias.isBefore(inicioMes)) {
                continue; 
            }

            LocalDate startCalc = inicioFerias.isBefore(inicioMes) ? inicioMes : inicioFerias;
            LocalDate endCalc = fimFerias.isAfter(fimMes) ? fimMes : fimFerias;

            long overlap = java.time.temporal.ChronoUnit.DAYS.between(startCalc, endCalc) + 1;
            diasFerias += (int) overlap;
        }
        return diasFerias;
    }
}
