package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AplicadorRegrasSazonaisServiceTest {

    @Mock private RegraNegocioDinamicaRepository regraRepository;
    @Mock private ComissaoCalculadaProporcionalRepository proporcionalRepository;
    @Mock private ComissaoCalculadaBaseRepository baseRepository;
    @Mock private BaseRHRepository rhRepository;
    @Mock private ComissaoCalculadaFinalRepository finalRepository;
    
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AplicadorRegrasSazonaisService service;

    private LocalDate dateRef;
    private ComissaoCalculadaProporcional prop;
    private ComissaoCalculadaBase base;
    private BaseRH rh;

    @BeforeEach
    void setUp() {
        dateRef = LocalDate.of(2025, 12, 1);
        
        rh = new BaseRH();
        rh.setMatricula("MAT-123");
        rh.setCodCargo(200);
        rh.setCodMarca(10);

        base = new ComissaoCalculadaBase();
        base.setId(1L);
        base.setMatricula("MAT-123");
        base.setValorBaseVendas(new BigDecimal("55000.00")); // Venda de 55.000
        base.setPercentualAplicado(new BigDecimal("0.05")); // 5%
        
        prop = new ComissaoCalculadaProporcional();
        prop.setId(10L);
        prop.setIdComissaoBase(1L);
        prop.setMatricula("MAT-123");
        prop.setDiasDoMes(31);
        prop.setDiasTrabalhados(31);
        prop.setValorComissaoProporcional(new BigDecimal("2750.00")); // 55000 * 5%
    }

    @Test
    void deveSomarBonusFixoNaComissaoFinal() {
        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.BONUS_FIXO);
        regra.setValorModificador(new BigDecimal("500.00"));
        regra.setCondicoesAplicacao("{\"matricula\":\"MAT-123\"}");

        when(regraRepository.findByMesCompetenciaAndStatusAprovacao("2025-12", StatusAprovacao.APROVADA))
                .thenReturn(Collections.singletonList(regra));
        when(proporcionalRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(prop));
        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));

        service.aplicarRegrasSazonais(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaFinal>> captor = ArgumentCaptor.forClass(List.class);
        verify(finalRepository).saveAll(captor.capture());

        ComissaoCalculadaFinal result = captor.getValue().get(0);
        // 2750 + 500 = 3250
        assertEquals(0, new BigDecimal("3250.00").compareTo(result.getValorComissaoFinal()));
        assertTrue(result.getHistoricoRegrasAplicadas().contains("BONUS_FIXO(+500.00)"));
    }

    @Test
    void deveSomarBonusNaBaseDeCalculoAntesDoPercentual() {
        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.BONUS_BASE);
        regra.setValorModificador(new BigDecimal("20000.00"));
        regra.setCondicoesAplicacao("{}"); // Aplica a todos

        when(regraRepository.findByMesCompetenciaAndStatusAprovacao("2025-12", StatusAprovacao.APROVADA))
                .thenReturn(Collections.singletonList(regra));
        when(proporcionalRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(prop));
        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));

        service.aplicarRegrasSazonais(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaFinal>> captor = ArgumentCaptor.forClass(List.class);
        verify(finalRepository).saveAll(captor.capture());

        ComissaoCalculadaFinal result = captor.getValue().get(0);
        // Bonus = 20000 * 5% = 1000. 
        // 2750 + 1000 = 3750.
        assertEquals(0, new BigDecimal("3750.00").compareTo(result.getValorComissaoFinal()));
        assertTrue(result.getHistoricoRegrasAplicadas().contains("BONUS_BASE(+1000.00)"));
    }

    @Test
    void deveAplicarFaixaDeVendasConformeEscalonamento() {
        // Criando 3 regras de faixa de vendas
        RegraNegocioDinamica regra40 = new RegraNegocioDinamica();
        regra40.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra40.setValorModificador(new BigDecimal("3500.00"));
        regra40.setCondicoesAplicacao("{\"minVendas\":40000}");

        RegraNegocioDinamica regra50 = new RegraNegocioDinamica();
        regra50.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra50.setValorModificador(new BigDecimal("4000.00"));
        regra50.setCondicoesAplicacao("{\"minVendas\":50000}");

        RegraNegocioDinamica regra60 = new RegraNegocioDinamica();
        regra60.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra60.setValorModificador(new BigDecimal("4500.00"));
        regra60.setCondicoesAplicacao("{\"minVendas\":60000}");

        when(regraRepository.findByMesCompetenciaAndStatusAprovacao("2025-12", StatusAprovacao.APROVADA))
                .thenReturn(Arrays.asList(regra40, regra50, regra60));
        when(proporcionalRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(prop));
        when(baseRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(base));
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.singletonList(rh));

        service.aplicarRegrasSazonais(dateRef);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaFinal>> captor = ArgumentCaptor.forClass(List.class);
        verify(finalRepository).saveAll(captor.capture());

        ComissaoCalculadaFinal result = captor.getValue().get(0);
        // Venda = 55.000. 
        // Bate na regra 40k (ganha 3500) e na regra 50k (ganha 4000). Nao bate na regra 60k.
        // O servico deve pegar a maior validada, que eh 4000.
        // 2750 + 4000 = 6750.
        assertEquals(0, new BigDecimal("6750.00").compareTo(result.getValorComissaoFinal()));
        assertTrue(result.getHistoricoRegrasAplicadas().contains("FAIXA_VENDAS(+4000.00)"));
    }
}
