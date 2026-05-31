package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.repository.AnomaliaRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaFinalRepository;
import br.com.ravision.backend.repository.ConfiguracaoAnomaliaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnomaliaServiceTest {

    @Mock
    private AnomaliaRepository anomaliaRepository;

    @Mock
    private ConfiguracaoAnomaliaRepository configuracaoRepository;

    @Mock
    private ComissaoCalculadaFinalRepository comissaoFinalRepository;

    @Mock
    private BaseRHRepository rhRepository;

    @InjectMocks
    private AnomaliaService anomaliaService;

    @Captor
    private ArgumentCaptor<List<Anomalia>> anomaliasCaptor;

    private LocalDate dateRef;
    private ConfiguracaoAnomalia configZScore;
    private ConfiguracaoAnomalia configIQR;

    @BeforeEach
    void setUp() {
        dateRef = LocalDate.of(2026, 5, 1);
        
        configZScore = ConfiguracaoAnomalia.builder()
                .algoritmo(TipoAlgoritmo.Z_SCORE)
                .limiteMultiplicador(2.0)
                .agruparPorCargo(false)
                .build();

        configIQR = ConfiguracaoAnomalia.builder()
                .algoritmo(TipoAlgoritmo.IQR)
                .limiteMultiplicador(1.5)
                .agruparPorCargo(true)
                .build();
    }

    @Test
    void dispararDeteccao_comNenhumDado_deveLancarExcecao() {
        when(configuracaoRepository.findTopByOrderByAtualizadoEmDesc()).thenReturn(Optional.of(configZScore));
        when(comissaoFinalRepository.findByDateRef(dateRef)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> {
            anomaliaService.dispararDeteccaoAnomalias(dateRef);
        });
        
        verify(anomaliaRepository).deletePendenteByDateRef(dateRef);
        verify(anomaliaRepository, never()).saveAll(any());
    }

    @Test
    void dispararDeteccao_zScore_deveDetectarAnomaliaAcimaEBaixoDaMedia() {
        // Para forçar anomalia, o outlier precisa ser bem extremo para não puxar o desvio padrão demais.
        // Vamos usar k=1.0 para o Z-score ficar mais sensivel e pegar os extremos.
        configZScore.setLimiteMultiplicador(1.0);
        when(configuracaoRepository.findTopByOrderByAtualizadoEmDesc()).thenReturn(Optional.of(configZScore));
        
        List<ComissaoCalculadaFinal> comissoes = Arrays.asList(
            criarComissao("1", "100"),   // Anomalia Abaixo
            criarComissao("2", "1000"),
            criarComissao("3", "1000"),
            criarComissao("4", "1000"),
            criarComissao("5", "1000"),
            criarComissao("6", "1000"),
            criarComissao("7", "5000")   // Anomalia Acima
        );

        when(comissaoFinalRepository.findByDateRef(dateRef)).thenReturn(comissoes);
        when(rhRepository.findByDateRef(dateRef)).thenReturn(Collections.emptyList());

        Map<String, Object> resultado = anomaliaService.dispararDeteccaoAnomalias(dateRef);

        verify(anomaliaRepository).saveAll(anomaliasCaptor.capture());
        List<Anomalia> salvas = anomaliasCaptor.getValue();
        
        assertEquals(1, salvas.size());
        assertEquals("Concluído", resultado.get("status"));
        assertEquals(1, resultado.get("anomaliasDetectadas"));

        assertTrue(salvas.stream().anyMatch(a -> a.getMatricula().equals("7") && a.getTipoAnomalia() == TipoAnomalia.MUITO_ACIMA_DA_MEDIA));
    }

    @Test
    void dispararDeteccao_IQR_deveDetectarComAgrupamentoDeCargo() {
        when(configuracaoRepository.findTopByOrderByAtualizadoEmDesc()).thenReturn(Optional.of(configIQR));

        // Simulando 2 cargos. Adicionando mais amostras normais para o Quartil 3 (Q3) não ser o próprio outlier.
        List<ComissaoCalculadaFinal> comissoes = Arrays.asList(
            criarComissao("A1", "1000"), criarComissao("A2", "1000"), 
            criarComissao("A3", "1050"), criarComissao("A4", "1050"), 
            criarComissao("A5", "1100"), criarComissao("A6", "1100"), 
            criarComissao("A7", "9000"), // A7 eh anomalia no cargo 1
            criarComissao("B1", "5000"), criarComissao("B2", "5100"), 
            criarComissao("B3", "5050"), criarComissao("B4", "5000")
        );

        List<BaseRH> rhList = Arrays.asList(
            criarRH("A1", 10), criarRH("A2", 10), criarRH("A3", 10), criarRH("A4", 10),
            criarRH("A5", 10), criarRH("A6", 10), criarRH("A7", 10),
            criarRH("B1", 20), criarRH("B2", 20), criarRH("B3", 20), criarRH("B4", 20)
        );

        when(comissaoFinalRepository.findByDateRef(dateRef)).thenReturn(comissoes);
        when(rhRepository.findByDateRef(dateRef)).thenReturn(rhList);

        anomaliaService.dispararDeteccaoAnomalias(dateRef);

        verify(anomaliaRepository).saveAll(anomaliasCaptor.capture());
        List<Anomalia> salvas = anomaliasCaptor.getValue();

        // Espera-se 1 anomalia (A7) do cargo 1
        assertEquals(1, salvas.size());
        assertEquals("A7", salvas.get(0).getMatricula());
        assertEquals(10, salvas.get(0).getCodCargo());
        assertEquals(TipoAnomalia.MUITO_ACIMA_DA_MEDIA, salvas.get(0).getTipoAnomalia());
    }

    private ComissaoCalculadaFinal criarComissao(String matricula, String valor) {
        ComissaoCalculadaFinal c = new ComissaoCalculadaFinal();
        c.setMatricula(matricula);
        c.setValorComissaoFinal(new BigDecimal(valor));
        return c;
    }

    private BaseRH criarRH(String matricula, Integer cargo) {
        BaseRH rh = new BaseRH();
        rh.setMatricula(matricula);
        rh.setCodCargo(cargo);
        return rh;
    }
}
