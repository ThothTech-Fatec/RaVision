package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.MonitoramentoIA;
import br.com.ravision.backend.dto.EstatisticaDiariaDTO;
import br.com.ravision.backend.dto.MonitoramentoAgregadoDTO;
import br.com.ravision.backend.dto.MonitoramentoIADTO;
import br.com.ravision.backend.repository.MonitoramentoIARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoramentoIAServiceTest {

    @Mock
    private MonitoramentoIARepository repository;

    @InjectMocks
    private MonitoramentoIAService service;

    private LocalDate dataInicio;
    private LocalDate dataFim;

    @BeforeEach
    void setUp() {
        dataInicio = LocalDate.of(2026, 5, 1);
        dataFim = LocalDate.of(2026, 5, 31);
    }

    @Test
    void salvarMetrica_devePersistirEntidadeCorretamente() {
        MonitoramentoIADTO dto = new MonitoramentoIADTO();
        dto.setPerguntaUsuario("Como calcular FGTS?");
        dto.setRespostaIA("Calcula-se...");
        dto.setTempoRespostaMs(1500L);
        dto.setUsuarioLogado("MAT123");

        service.salvarMetrica(dto);

        ArgumentCaptor<MonitoramentoIA> captor = ArgumentCaptor.forClass(MonitoramentoIA.class);
        verify(repository, times(1)).save(captor.capture());

        MonitoramentoIA entidadeSalva = captor.getValue();
        assertEquals("Como calcular FGTS?", entidadeSalva.getPerguntaUsuario());
        assertEquals(1500L, entidadeSalva.getTempoRespostaMs());
        assertEquals("MAT123", entidadeSalva.getUsuarioLogado());
        assertTrue(entidadeSalva.getSucessoFerramenta()); // Verifica valor default
        assertNotNull(entidadeSalva.getTimestamp());
    }

    @Test
    void buscarMetricasAgregadas_deveRetornarDadosAgregados() {
        LocalDateTime start = dataInicio.atStartOfDay();
        LocalDateTime end = dataFim.atTime(java.time.LocalTime.MAX);

        when(repository.countByTimestampBetween(start, end)).thenReturn(150L);
        when(repository.averageTempoRespostaBetween(start, end)).thenReturn(2100.5);

        List<EstatisticaDiariaDTO> mockEstatisticas = Arrays.asList(
                new EstatisticaDiariaDTO(LocalDate.of(2026, 5, 1), 50L, 2000.0)
        );
        when(repository.findEstatisticasDiariasBetween(start, end)).thenReturn(mockEstatisticas);

        MonitoramentoIA mockLog = new MonitoramentoIA();
        mockLog.setPerguntaUsuario("Teste");
        mockLog.setTempoRespostaMs(200L);
        mockLog.setTimestamp(LocalDateTime.now());
        when(repository.findByTimestampBetweenOrderByTimestampDesc(start, end)).thenReturn(Arrays.asList(mockLog));

        MonitoramentoAgregadoDTO resultado = service.buscarMetricasAgregadas(dataInicio, dataFim);

        assertNotNull(resultado);
        assertEquals(150L, resultado.getTotalRequisicoes());
        assertEquals(2100.5, resultado.getTempoMedioGeralMs());
        assertEquals(1, resultado.getEstatisticasDiarias().size());
        assertEquals(1, resultado.getLogsRecentes().size());
        
        verify(repository).countByTimestampBetween(start, end);
        verify(repository).averageTempoRespostaBetween(start, end);
    }
}
