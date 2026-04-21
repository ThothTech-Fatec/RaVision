package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.domain.ComissaoCalculadaProporcional;
import br.com.ravision.backend.domain.IntercorrenciaRH;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaProporcionalRepository;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import org.junit.jupiter.api.BeforeEach;
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

    private LocalDate dateRef;
    private BaseRH rh;
    private ComissaoCalculadaBase base;

    @BeforeEach
    void setUp() {
        dateRef = LocalDate.of(2025, 11, 1); // Novembro tem 30 dias
        
        rh = new BaseRH();
        rh.setMatricula("MAT-123");
        
        base = new ComissaoCalculadaBase();
        base.setId(1L);
        base.setMatricula("MAT-123");
        base.setCodLoja(10); // Loja Principal
        base.setValorBaseVendas(new BigDecimal("100000.00"));
        base.setPercentualAplicado(new BigDecimal("0.05"));
        base.setValorComissaoGerado(new BigDecimal("2000.00"));
    }

    @Test
    void deveCalcularAfastamentoMenorQue15DiasPagandoProporcional() {
        // Afastamento 10 dias -> Trabalhou 20
        // Media Diaria Vendas: 100k / 20 = 5000
        // Base Adicional: 5000 * 10 = 50000
        // Comissao Adicional: 50000 * 5% = 2500
        // Total Comissao = 2000 (real) + 2500 = 4500
        // Como 4500 > 3500 (Piso), paga 4500.

        IntercorrenciaRH atestado = new IntercorrenciaRH();
        atestado.setMatricula("MAT-123");
        atestado.setTipo("ATESTADO");
        atestado.setDataInicio(LocalDate.of(2025, 11, 1));
        atestado.setDataFim(LocalDate.of(2025, 11, 10)); // 10 dias

        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.singletonList(atestado));

        service.calcularProporcional(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captor = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captor.capture());

        ComissaoCalculadaProporcional result = captor.getValue().get(0);
        
        assertEquals("AFASTAMENTO", result.getMotivoProporcionalidade());
        assertEquals(20, result.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("4500.00").compareTo(result.getValorComissaoProporcional()));
    }

    @Test
    void deveCalcularAfastamentoMenorQue15DiasGarantindoPiso3500() {
        // Ajustando base para forcar o valor a ficar abaixo do piso
        base.setValorBaseVendas(new BigDecimal("40000.00"));
        base.setValorComissaoGerado(new BigDecimal("800.00"));
        
        // Afastamento 10 dias -> Trabalhou 20
        // Media Diaria Vendas: 40k / 20 = 2000
        // Base Adicional: 2000 * 10 = 20000
        // Comissao Adicional: 20000 * 5% = 1000
        // Total Comissao = 800 (real) + 1000 = 1800
        // Como 1800 < 3500 (Piso), DEVE PAGAR 3500.

        IntercorrenciaRH atestado = new IntercorrenciaRH();
        atestado.setMatricula("MAT-123");
        atestado.setTipo("ATESTADO");
        atestado.setDataInicio(LocalDate.of(2025, 11, 1));
        atestado.setDataFim(LocalDate.of(2025, 11, 10)); // 10 dias

        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.singletonList(atestado));

        service.calcularProporcional(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captor = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captor.capture());

        ComissaoCalculadaProporcional result = captor.getValue().get(0);
        
        assertEquals("AFASTAMENTO", result.getMotivoProporcionalidade());
        assertEquals(20, result.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("3500.00").compareTo(result.getValorComissaoProporcional()));
    }

    @Test
    void deveCalcularAfastamentoMaiorQue15DiasLimitandoPagamento() {
        // Afastamento 20 dias -> Trabalhou 10
        // Media Diaria Vendas: 100k / 10 = 10000
        // Base Adicional Limitada (Empresa so paga 15): 10000 * 15 = 150000
        // Comissao Adicional: 150000 * 5% = 7500
        // Total Comissao = 2000 (real) + 7500 = 9500

        IntercorrenciaRH atestado = new IntercorrenciaRH();
        atestado.setMatricula("MAT-123");
        atestado.setTipo("ATESTADO");
        atestado.setDataInicio(LocalDate.of(2025, 11, 1));
        atestado.setDataFim(LocalDate.of(2025, 11, 20)); // 20 dias

        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.singletonList(atestado));

        service.calcularProporcional(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captor = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captor.capture());

        ComissaoCalculadaProporcional result = captor.getValue().get(0);
        
        assertEquals("AFASTAMENTO", result.getMotivoProporcionalidade());
        assertEquals(10, result.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("9500.00").compareTo(result.getValorComissaoProporcional()));
    }

    @Test
    void deveDividirComissaoParaFuncionarioDeMultiplasLojas() {
        // Funcionario transferido, passou 10 dias na LOJA-55
        // Total comissao: 3000
        // Loja Secundaria (10 dias): 3000 * (10/30) = 1000
        // Loja Principal (20 dias): 3000 * (20/30) = 2000
        base.setValorComissaoGerado(new BigDecimal("3000.00"));

        IntercorrenciaRH transferencia = new IntercorrenciaRH();
        transferencia.setMatricula("MAT-123");
        transferencia.setTipo("TRANSFERENCIA");
        transferencia.setDataInicio(LocalDate.of(2025, 11, 1));
        transferencia.setDataFim(LocalDate.of(2025, 11, 10)); // 10 dias
        transferencia.setCodLojaSecundaria(55);

        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.singletonList(transferencia));

        service.calcularProporcional(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captor = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captor.capture());

        List<ComissaoCalculadaProporcional> results = captor.getValue();
        assertEquals(2, results.size());

        // Loja Principal
        ComissaoCalculadaProporcional lojaPrincipal = results.get(0);
        assertEquals("RATEIO_LOJA_PRINCIPAL", lojaPrincipal.getMotivoProporcionalidade());
        assertEquals(10, lojaPrincipal.getCodLoja());
        assertEquals(20, lojaPrincipal.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("2000.00").compareTo(lojaPrincipal.getValorComissaoProporcional()));

        // Loja Secundaria
        ComissaoCalculadaProporcional lojaSec = results.get(1);
        assertEquals("RATEIO_LOJA_SECUNDARIA", lojaSec.getMotivoProporcionalidade());
        assertEquals(55, lojaSec.getCodLoja());
        assertEquals(10, lojaSec.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("1000.00").compareTo(lojaSec.getValorComissaoProporcional()));
    }

    @Test
    void deveGarantirIsencaoDePerdasParaLicencaMaternidade() {
        // Licenca maternidade garante isencao total (dias trabalhados = integral e comissao = original)
        base.setValorComissaoGerado(new BigDecimal("5000.00"));

        IntercorrenciaRH maternidade = new IntercorrenciaRH();
        maternidade.setMatricula("MAT-123");
        maternidade.setTipo("LICENCA_MATERNIDADE");
        maternidade.setDataInicio(LocalDate.of(2025, 11, 5));
        maternidade.setDataFim(LocalDate.of(2025, 11, 25));

        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));
        when(intercorrenciaRepository.findAll()).thenReturn(Collections.singletonList(maternidade));

        service.calcularProporcional(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaProporcional>> captor = ArgumentCaptor.forClass(List.class);
        verify(proporcionalRepository).saveAll(captor.capture());

        ComissaoCalculadaProporcional result = captor.getValue().get(0);
        
        assertEquals("LICENCA_MATERNIDADE", result.getMotivoProporcionalidade());
        assertEquals(30, result.getDiasTrabalhados());
        assertEquals(0, new BigDecimal("5000.00").compareTo(result.getValorComissaoProporcional()));
    }
}
