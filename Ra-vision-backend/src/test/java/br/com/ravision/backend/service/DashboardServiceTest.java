package br.com.ravision.backend.service;

import br.com.ravision.backend.dto.DashboardAgregacaoDTO;
import br.com.ravision.backend.dto.DashboardTotalDTO;
import br.com.ravision.backend.repository.ComissaoCalculadaFinalRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import br.com.ravision.backend.repository.IntercorrenciaRHRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.dto.ExecutivaKPIsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ComissaoCalculadaFinalRepository comissaoRepository;

    @Mock
    private BaseVendasRepository baseVendasRepository;

    @Mock
    private IntercorrenciaRHRepository intercorrenciaRHRepository;

    @Mock
    private BaseRHRepository baseRHRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private LocalDate dataRef;

    @BeforeEach
    void setUp() {
        dataRef = LocalDate.of(2025, 11, 1);
    }

    @Test
    void getComissoesPorLoja_deveRetornarAgregacaoCorreta() {
        // Arrange
        List<DashboardAgregacaoDTO> mockList = Arrays.asList(
                new DashboardAgregacaoDTO("Loja Centro", new BigDecimal("15000.00")),
                new DashboardAgregacaoDTO("Loja Norte", new BigDecimal("5000.00"))
        );
        when(comissaoRepository.sumarizarComissoesPorLoja(dataRef)).thenReturn(mockList);

        // Act
        List<DashboardAgregacaoDTO> result = dashboardService.getComissoesPorLoja(dataRef);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Loja Centro", result.get(0).getChave());
        assertEquals(new BigDecimal("15000.00"), result.get(0).getValor());
        verify(comissaoRepository, times(1)).sumarizarComissoesPorLoja(dataRef);
    }

    @Test
    void getComissoesPorLoja_deveRetornarListaVaziaSeNaoHouverDados() {
        // Arrange
        when(comissaoRepository.sumarizarComissoesPorLoja(dataRef)).thenReturn(Collections.emptyList());

        // Act
        List<DashboardAgregacaoDTO> result = dashboardService.getComissoesPorLoja(dataRef);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getComissoesPorMarca_deveRetornarAgregacaoCorreta() {
        // Arrange
        List<DashboardAgregacaoDTO> mockList = Arrays.asList(
                new DashboardAgregacaoDTO("Marca A", new BigDecimal("20000.00"))
        );
        when(comissaoRepository.sumarizarComissoesPorMarca(dataRef)).thenReturn(mockList);

        // Act
        List<DashboardAgregacaoDTO> result = dashboardService.getComissoesPorMarca(dataRef);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Marca A", result.get(0).getChave());
        assertEquals(new BigDecimal("20000.00"), result.get(0).getValor());
        verify(comissaoRepository, times(1)).sumarizarComissoesPorMarca(dataRef);
    }

    @Test
    void getTotalComissaoGeral_deveRetornarSomaCorreta() {
        // Arrange
        when(comissaoRepository.sumarizarComissaoGeral(dataRef)).thenReturn(new BigDecimal("25000.00"));

        // Act
        DashboardTotalDTO result = dashboardService.getTotalComissaoGeral(dataRef);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("25000.00"), result.getTotal());
        verify(comissaoRepository, times(1)).sumarizarComissaoGeral(dataRef);
    }

    @Test
    void getTotalComissaoGeral_deveRetornarZeroQuandoNulo() {
        // Arrange
        when(comissaoRepository.sumarizarComissaoGeral(dataRef)).thenReturn(null);

        // Act
        DashboardTotalDTO result = dashboardService.getTotalComissaoGeral(dataRef);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotal());
    }

    @Test
    void getKPIsExecutiva_deveRetornarDadosCorretos() {
        when(baseVendasRepository.sumarizarFaturamentoGeral(dataRef)).thenReturn(new BigDecimal("100000.00"));
        when(comissaoRepository.sumarizarComissaoGeral(dataRef)).thenReturn(new BigDecimal("5000.00"));

        ExecutivaKPIsDTO result = dashboardService.getKPIsExecutiva(dataRef);

        assertNotNull(result);
        assertEquals(new BigDecimal("100000.00"), result.getFaturamentoAtual());
        assertEquals(new BigDecimal("5000.00"), result.getComissoesAtuais());
        assertEquals(5.0, result.getCustoComissaoPercentual());
    }
}
