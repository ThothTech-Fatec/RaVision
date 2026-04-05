package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.domain.ComissaoCalculadaProporcional;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaProporcionalRepository;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculoProporcionalServiceTest {

    @Mock private ComissaoCalculadaBaseRepository baseRepository;
    @Mock private BaseRHRepository rhRepository;
    @Mock private IntercorrenciaRHRepository intercorrenciaRepository;
    @Mock private ComissaoCalculadaProporcionalRepository proporcionalRepository;

    @InjectMocks
    private CalculoProporcionalService service;

    @Test
    void garanteMatematicaDeProporcionalidade() {
        LocalDate dateRefOutubro = LocalDate.of(2025, 10, 1);
        LocalDate dateRefNovembro = LocalDate.of(2025, 11, 1);

        // --- Cenario Admissao ---
        // Vendedor Admitido em 10/10/2025 -> Trabalhou 31 - 10 = 21 dias 
        BaseRH rhAdmitido = new BaseRH();
        rhAdmitido.setMatricula("V_ADM");
        rhAdmitido.setDataAdmissao(LocalDate.of(2025, 10, 10));

        ComissaoCalculadaBase comissaoAdmitido = new ComissaoCalculadaBase();
        comissaoAdmitido.setId(1L);
        comissaoAdmitido.setMatricula("V_ADM");
        comissaoAdmitido.setValorComissaoGerado(new BigDecimal("1000.00")); // 1000 / 31 * 21 = 677.42

        // --- Execucao Outubro ---
        when(baseRepository.findByDateRef(dateRefOutubro)).thenReturn(Collections.singletonList(comissaoAdmitido));
        when(rhRepository.findByDateRef(dateRefOutubro)).thenReturn(Collections.singletonList(rhAdmitido));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.emptyList());

        service.calcularProporcional(dateRefOutubro);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captorOut = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captorOut.capture());

        ComissaoCalculadaProporcional propAdm = captorOut.getValue().get(0);
        assertEquals("ADMISSAO", propAdm.getMotivoProporcionalidade());
        assertEquals(31, propAdm.getDiasDoMes());
        assertEquals(21, propAdm.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("677.42").compareTo(propProp_Comparativo(propAdm)));

        // --- Cenario Demissao ---
        // Vendedor Demitido em 10/11/2025 -> Trabalhou 10 dias do total de 30
        BaseRH rhDemitido = new BaseRH();
        rhDemitido.setMatricula("V_DEM");
        rhDemitido.setDataDemissao(LocalDate.of(2025, 11, 10));

        ComissaoCalculadaBase comissaoDemitido = new ComissaoCalculadaBase();
        comissaoDemitido.setId(2L);
        comissaoDemitido.setMatricula("V_DEM");
        comissaoDemitido.setValorComissaoGerado(new BigDecimal("3000.00")); // 3000 / 30 * 10 = 1000.00

        // --- Execucao Novembro ---
        when(baseRepository.findByDateRef(dateRefNovembro)).thenReturn(Collections.singletonList(comissaoDemitido));
        when(rhRepository.findByDateRef(dateRefNovembro)).thenReturn(Collections.singletonList(rhDemitido));

        service.calcularProporcional(dateRefNovembro);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captorNov = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captorNov.capture()); // O captor pega ambas chamadas

        ComissaoCalculadaProporcional propDem = captorNov.getAllValues().get(1).get(0); 

        assertEquals("DEMISSAO", propDem.getMotivoProporcionalidade());
        assertEquals(30, propDem.getDiasDoMes());
        assertEquals(10, propDem.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("1000.00").compareTo(propDem.getValorComissaoProporcional()));
    }
    
    // Auxiliar apenas pra Mockito assertion bypass com BigDecimal
    public BigDecimal propProp_Comparativo(ComissaoCalculadaProporcional cp) { return cp.getValorComissaoProporcional();}
}
