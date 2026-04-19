package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.dto.SimulacaoResponseDTO;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import br.com.ravision.backend.repository.RegraNegocioDinamicaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulacaoServiceTest {

    @Mock
    private RegraNegocioDinamicaRepository regraRepository;

    @Mock
    private ComissaoCalculadaBaseRepository comissaoRepository;

    @Mock
    private BaseRHRepository baseRHRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SimulacaoService simulacaoService;

    private RegraNegocioDinamica regraBonusFixo;
    private RegraNegocioDinamica regraOverridePercentual;
    private RegraNegocioDinamica regraBlackFriday;
    private RegraNegocioDinamica regraFaixaVendas;

    private ComissaoCalculadaBase comissao1;
    private ComissaoCalculadaBase comissao2;
    private ComissaoCalculadaBase comissao3;

    private final LocalDate DATE_REF = LocalDate.of(2025, 3, 1);

    @BeforeEach
    void setUp() {
        // ── Regras ──────────────────────────────────────────────────
        regraBonusFixo = RegraNegocioDinamica.builder()
                .id(1L)
                .descricaoRegra("Bônus de performance Q1")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2025-03")
                .condicoesAplicacao("{}")
                .valorModificador(new BigDecimal("500.00"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("gestor_rh")
                .build();

        regraOverridePercentual = RegraNegocioDinamica.builder()
                .id(2L)
                .descricaoRegra("Novo percentual para cargo 100")
                .tipoRegra(TipoRegra.OVERRIDE_PERCENTUAL)
                .mesCompetencia("2025-03")
                .condicoesAplicacao("{\"codCargo\": 100}")
                .valorModificador(new BigDecimal("5.00"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("gestor_rh")
                .build();

        regraBlackFriday = RegraNegocioDinamica.builder()
                .id(3L)
                .descricaoRegra("Multiplicador Black Friday")
                .tipoRegra(TipoRegra.BLACK_FRIDAY)
                .mesCompetencia("2025-03")
                .condicoesAplicacao("{\"codMarca\": 40}")
                .valorModificador(new BigDecimal("1.50"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("gestor_rh")
                .build();

        regraFaixaVendas = RegraNegocioDinamica.builder()
                .id(4L)
                .descricaoRegra("Bônus por faixa de vendas alta")
                .tipoRegra(TipoRegra.FAIXA_VENDAS)
                .mesCompetencia("2025-03")
                .condicoesAplicacao("{}")
                .valorModificador(new BigDecimal("200.00"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("gestor_rh")
                .build();

        // ── Comissões Base ──────────────────────────────────────────
        comissao1 = new ComissaoCalculadaBase();
        comissao1.setId(1L);
        comissao1.setDateRef(DATE_REF);
        comissao1.setMatricula("MATRIC-001");
        comissao1.setCodLoja(5);
        comissao1.setCodCargo(100);
        comissao1.setValorBaseVendas(new BigDecimal("50000.00"));
        comissao1.setPercentualAplicado(new BigDecimal("2.50"));
        comissao1.setValorComissaoGerado(new BigDecimal("1250.00"));

        comissao2 = new ComissaoCalculadaBase();
        comissao2.setId(2L);
        comissao2.setDateRef(DATE_REF);
        comissao2.setMatricula("MATRIC-002");
        comissao2.setCodLoja(5);
        comissao2.setCodCargo(100);
        comissao2.setValorBaseVendas(new BigDecimal("30000.00"));
        comissao2.setPercentualAplicado(new BigDecimal("2.50"));
        comissao2.setValorComissaoGerado(new BigDecimal("750.00"));

        comissao3 = new ComissaoCalculadaBase();
        comissao3.setId(3L);
        comissao3.setDateRef(DATE_REF);
        comissao3.setMatricula("MATRIC-003");
        comissao3.setCodLoja(10);
        comissao3.setCodCargo(150);
        comissao3.setValorBaseVendas(new BigDecimal("80000.00"));
        comissao3.setPercentualAplicado(new BigDecimal("1.00"));
        comissao3.setValorComissaoGerado(new BigDecimal("800.00"));
    }

    // ─── TESTE 1: BONUS_FIXO para todos ─────────────────────────────────────────

    @Test
    @DisplayName("Simular BONUS_FIXO sem filtro deve somar bônus a todos os funcionários")
    void simularBonusFixoDeveCalcularImpactoCorreto() {
        // Arrange
        List<ComissaoCalculadaBase> comissoes = Arrays.asList(comissao1, comissao2, comissao3);

        when(regraRepository.findById(1L)).thenReturn(Optional.of(regraBonusFixo));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(comissoes);

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(1L);

        // Assert
        // Custo Atual: 1250 + 750 + 800 = 2800
        assertEquals(new BigDecimal("2800.00"), resultado.getCustoTotalAtual());

        // Custo Simulado: (1250+500) + (750+500) + (800+500) = 1750 + 1250 + 1300 = 4300
        assertEquals(new BigDecimal("4300.00"), resultado.getCustoTotalSimulado());

        // Impacto: 4300 - 2800 = 1500
        BigDecimal impactoEsperado = resultado.getCustoTotalSimulado().subtract(resultado.getCustoTotalAtual());
        assertEquals(impactoEsperado, resultado.getImpactoFinanceiro());
        assertEquals(new BigDecimal("1500.00"), resultado.getImpactoFinanceiro());

        // Todos os 3 funcionários são afetados
        assertEquals(3, resultado.getQuantidadeFuncionariosAfetados());
        assertEquals(1L, resultado.getRegraId());

        verify(comissaoRepository, never()).save(any());
        verify(comissaoRepository, never()).saveAll(any());
    }

    // ─── TESTE 2: OVERRIDE_PERCENTUAL com filtro por codCargo ─────────────────

    @Test
    @DisplayName("Simular OVERRIDE_PERCENTUAL deve recalcular comissão com novo percentual para funcionários filtrados")
    void simularOverridePercentualDeveRecalcularComissao() {
        // Arrange: regra aplica novo percentual 5% apenas para codCargo=100
        List<ComissaoCalculadaBase> comissoes = Arrays.asList(comissao1, comissao2, comissao3);

        when(regraRepository.findById(2L)).thenReturn(Optional.of(regraOverridePercentual));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(comissoes);

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(2L);

        // Assert
        // Custo Atual: 1250 + 750 + 800 = 2800
        assertEquals(new BigDecimal("2800.00"), resultado.getCustoTotalAtual());

        // comissao1 (codCargo=100): 50000 * 5/100 = 2500.00
        // comissao2 (codCargo=100): 30000 * 5/100 = 1500.00
        // comissao3 (codCargo=150): mantém 800.00 (não atende filtro)
        // Custo Simulado: 2500 + 1500 + 800 = 4800
        assertEquals(new BigDecimal("4800.00"), resultado.getCustoTotalSimulado());

        // Impacto: 4800 - 2800 = 2000
        assertEquals(new BigDecimal("2000.00"), resultado.getImpactoFinanceiro());

        // Apenas 2 funcionários afetados (codCargo=100)
        assertEquals(2, resultado.getQuantidadeFuncionariosAfetados());
    }

    // ─── TESTE 3: Regra NÃO PENDENTE deve lançar exceção ─────────────────────

    @Test
    @DisplayName("Simular regra com status APROVADA deve lançar exceção")
    void simularRegraNaoPendenteDeveLancarExcecao() {
        // Arrange
        RegraNegocioDinamica regraAprovada = RegraNegocioDinamica.builder()
                .id(10L)
                .descricaoRegra("Regra já aprovada")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2025-03")
                .valorModificador(new BigDecimal("100.00"))
                .statusAprovacao(StatusAprovacao.APROVADA)
                .criadoPor("gestor_rh")
                .build();

        when(regraRepository.findById(10L)).thenReturn(Optional.of(regraAprovada));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> simulacaoService.simular(10L));

        assertTrue(exception.getMessage().contains("PENDENTE"));
        verify(comissaoRepository, never()).findByDateRef(any());
    }

    // ─── TESTE 4: Regra inexistente deve lançar exceção ──────────────────────

    @Test
    @DisplayName("Simular regra com ID inexistente deve lançar exceção")
    void simularRegraInexistenteDeveLancarExcecao() {
        // Arrange
        when(regraRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> simulacaoService.simular(999L));

        assertTrue(exception.getMessage().contains("não encontrada"));
    }

    // ─── TESTE 5: Sem comissões base deve retornar zeros ─────────────────────

    @Test
    @DisplayName("Simular sem comissões base deve retornar todos os valores como zero")
    void simularSemComissoesBaseDeveRetornarZeros() {
        // Arrange
        when(regraRepository.findById(1L)).thenReturn(Optional.of(regraBonusFixo));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(Collections.emptyList());

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(1L);

        // Assert
        assertEquals(BigDecimal.ZERO, resultado.getCustoTotalAtual());
        assertEquals(BigDecimal.ZERO, resultado.getCustoTotalSimulado());
        assertEquals(BigDecimal.ZERO, resultado.getImpactoFinanceiro());
        assertEquals(0, resultado.getQuantidadeFuncionariosAfetados());
    }

    // ─── TESTE 6: Filtro por matrícula deve afetar apenas 1 funcionário ──────

    @Test
    @DisplayName("Simular com filtro por matrícula deve afetar apenas o funcionário correto")
    void simularComFiltroMatriculaDeveAfetarApenasFuncionarioCorreto() {
        // Arrange: Bônus fixo apenas para MATRIC-001
        RegraNegocioDinamica regraFiltrada = RegraNegocioDinamica.builder()
                .id(5L)
                .descricaoRegra("Bônus individual")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2025-03")
                .condicoesAplicacao("{\"matricula\": \"MATRIC-001\"}")
                .valorModificador(new BigDecimal("1000.00"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("gestor_rh")
                .build();

        List<ComissaoCalculadaBase> comissoes = Arrays.asList(comissao1, comissao2, comissao3);

        when(regraRepository.findById(5L)).thenReturn(Optional.of(regraFiltrada));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(comissoes);

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(5L);

        // Assert
        // Custo Atual: 1250 + 750 + 800 = 2800
        assertEquals(new BigDecimal("2800.00"), resultado.getCustoTotalAtual());

        // Custo Simulado: (1250+1000) + 750 + 800 = 2250 + 750 + 800 = 3800
        assertEquals(new BigDecimal("3800.00"), resultado.getCustoTotalSimulado());

        // Impacto: 3800 - 2800 = 1000
        assertEquals(new BigDecimal("1000.00"), resultado.getImpactoFinanceiro());

        // Apenas 1 funcionário afetado
        assertEquals(1, resultado.getQuantidadeFuncionariosAfetados());
    }

    // ─── TESTE 7: BLACK_FRIDAY deve multiplicar comissão ─────────────────────

    @Test
    @DisplayName("Simular BLACK_FRIDAY deve multiplicar comissão pelo fator para funcionários da marca filtrada")
    void simularBlackFridayDeveMultiplicarComissao() {
        // Arrange: multiplicador 1.5x para codMarca=40
        List<ComissaoCalculadaBase> comissoes = Arrays.asList(comissao1, comissao2);

        when(regraRepository.findById(3L)).thenReturn(Optional.of(regraBlackFriday));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(comissoes);

        // BaseRH: MATRIC-001 pertence à marca 40, MATRIC-002 à marca 50
        BaseRH rh1 = new BaseRH(1L, DATE_REF, 40, "Marca A", 5, "Loja A",
                "MATRIC-001", DATE_REF.minusYears(2), null, 100, "Vendedor");
        BaseRH rh2 = new BaseRH(2L, DATE_REF, 50, "Marca B", 5, "Loja A",
                "MATRIC-002", DATE_REF.minusYears(1), null, 100, "Vendedor");

        when(baseRHRepository.findByDateRef(DATE_REF)).thenReturn(Arrays.asList(rh1, rh2));

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(3L);

        // Assert
        // Custo Atual: 1250 + 750 = 2000
        assertEquals(new BigDecimal("2000.00"), resultado.getCustoTotalAtual());

        // MATRIC-001 (marca 40): 1250 * 1.5 = 1875.00
        // MATRIC-002 (marca 50): mantém 750.00 (não atende filtro codMarca=40)
        // Custo Simulado: 1875 + 750 = 2625
        assertEquals(new BigDecimal("2625.00"), resultado.getCustoTotalSimulado());

        // Impacto: 2625 - 2000 = 625
        assertEquals(new BigDecimal("625.00"), resultado.getImpactoFinanceiro());

        // Apenas 1 funcionário afetado (MATRIC-001 da marca 40)
        assertEquals(1, resultado.getQuantidadeFuncionariosAfetados());
    }

    // ─── TESTE 8: FAIXA_VENDAS deve aplicar bônus ─────────────────────────────

    @Test
    @DisplayName("Simular FAIXA_VENDAS deve aplicar bônus a todos os funcionários quando sem filtro")
    void simularFaixaVendasDeveAplicarBonusPorFaixa() {
        // Arrange
        List<ComissaoCalculadaBase> comissoes = Arrays.asList(comissao1, comissao2);

        when(regraRepository.findById(4L)).thenReturn(Optional.of(regraFaixaVendas));
        when(comissaoRepository.findByDateRef(DATE_REF)).thenReturn(comissoes);

        // Act
        SimulacaoResponseDTO resultado = simulacaoService.simular(4L);

        // Assert
        // Custo Atual: 1250 + 750 = 2000
        assertEquals(new BigDecimal("2000.00"), resultado.getCustoTotalAtual());

        // FAIXA_VENDAS soma valorModificador como bônus
        // comissao1: 1250 + 200 = 1450
        // comissao2: 750 + 200 = 950
        // Custo Simulado: 1450 + 950 = 2400
        assertEquals(new BigDecimal("2400.00"), resultado.getCustoTotalSimulado());

        // Impacto: 2400 - 2000 = 400
        BigDecimal impactoEsperado = resultado.getCustoTotalSimulado().subtract(resultado.getCustoTotalAtual());
        assertEquals(impactoEsperado, resultado.getImpactoFinanceiro());
        assertEquals(new BigDecimal("400.00"), resultado.getImpactoFinanceiro());

        // Ambos funcionários afetados
        assertEquals(2, resultado.getQuantidadeFuncionariosAfetados());
    }

    // ─── TESTES ADICIONAIS DE COBERTURA ──────────────────────────────────────────

    @Test
    @DisplayName("Converter mesCompetencia deve transformar 'yyyy-MM' em LocalDate do primeiro dia")
    void converterMesCompetenciaDeveRetornarPrimeiroDiaDoMes() {
        LocalDate resultado = simulacaoService.converterMesCompetencia("2025-03");
        assertEquals(LocalDate.of(2025, 3, 1), resultado);

        LocalDate resultado2 = simulacaoService.converterMesCompetencia("2024-12");
        assertEquals(LocalDate.of(2024, 12, 1), resultado2);
    }

    @Test
    @DisplayName("Parsear condições nulas ou vazias deve retornar Map vazio")
    void parsearCondicoesVaziasDeveRetornarMapVazio() {
        assertTrue(simulacaoService.parsearCondicoes(null).isEmpty());
        assertTrue(simulacaoService.parsearCondicoes("").isEmpty());
        assertTrue(simulacaoService.parsearCondicoes("{}").isEmpty());
        assertTrue(simulacaoService.parsearCondicoes("   ").isEmpty());
    }

    @Test
    @DisplayName("Parsear condições com JSON válido deve retornar Map com filtros")
    void parsearCondicoesValidasDeveRetornarMapCorreto() {
        var resultado = simulacaoService.parsearCondicoes("{\"matricula\": \"MATRIC-001\", \"codCargo\": 100}");
        assertEquals("MATRIC-001", resultado.get("matricula"));
        assertEquals(100, resultado.get("codCargo"));
    }

    @Test
    @DisplayName("Parsear condições com JSON inválido deve retornar Map vazio sem erro")
    void parsearCondicoesInvalidasDeveRetornarMapVazio() {
        var resultado = simulacaoService.parsearCondicoes("json_invalido{{{");
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Verificar condições com filtro codLoja deve filtrar corretamente")
    void verificarCondicoesCodLojaDeveFiltrarCorretamente() {
        var condicoes = simulacaoService.parsearCondicoes("{\"codLoja\": 5}");

        // comissao1 tem codLoja=5 → deve passar
        assertTrue(simulacaoService.verificarCondicoes(comissao1, condicoes, Collections.emptyMap()));

        // comissao3 tem codLoja=10 → deve falhar
        assertFalse(simulacaoService.verificarCondicoes(comissao3, condicoes, Collections.emptyMap()));
    }

    @Test
    @DisplayName("Simular regra com status RECUSADA deve lançar exceção")
    void simularRegraRecusadaDeveLancarExcecao() {
        RegraNegocioDinamica regraRecusada = RegraNegocioDinamica.builder()
                .id(11L)
                .descricaoRegra("Regra recusada")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2025-03")
                .valorModificador(new BigDecimal("100.00"))
                .statusAprovacao(StatusAprovacao.RECUSADA)
                .criadoPor("gestor_rh")
                .build();

        when(regraRepository.findById(11L)).thenReturn(Optional.of(regraRecusada));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> simulacaoService.simular(11L));

        assertTrue(exception.getMessage().contains("PENDENTE"));
    }
}
