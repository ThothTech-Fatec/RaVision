package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.*;
import br.com.ravision.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class AplicadorRegrasSazonaisIntegrationTest {

    @Autowired private AplicadorRegrasSazonaisService service;
    @Autowired private RegraNegocioDinamicaRepository regraRepository;
    @Autowired private ComissaoCalculadaProporcionalRepository proporcionalRepository;
    @Autowired private ComissaoCalculadaBaseRepository baseRepository;
    @Autowired private BaseRHRepository rhRepository;
    @Autowired private ComissaoCalculadaFinalRepository finalRepository;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        try {
            jdbcTemplate.execute("ALTER TABLE tb_regra_negocio_dinamica DROP CONSTRAINT IF EXISTS tb_regra_negocio_dinamica_tipo_regra_check");
        } catch (Exception e) {
            // Ignora se nao existir ou for H2
        }
        regraRepository.deleteAll();
        proporcionalRepository.deleteAll();
        baseRepository.deleteAll();
        rhRepository.deleteAll();
        finalRepository.deleteAll();
    }

    @Test
    @DisplayName("Cenário 1: deveAplicarBonusFixoParaMatric134EmAgosto (Bônus Nominal)")
    void deveAplicarBonusFixoParaMatric134EmAgosto() {
        LocalDate dateRef = LocalDate.of(2025, 8, 1);
        String matricula = "MATRIC-134";

        setupData(dateRef, matricula, 200, 10, new BigDecimal("100000.00"), new BigDecimal("0.05"), new BigDecimal("5000.00"));

        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.BONUS_FIXO);
        regra.setMesCompetencia("2025-08");
        regra.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra.setCondicoesAplicacao("{\"matricula\": \"MATRIC-134\"}");
        regra.setValorModificador(new BigDecimal("500.00"));
        regra.setDescricaoRegra("Bonus de Agosto");
        regra.setCriadoPor("SISTEMA");
        regraRepository.save(regra);

        service.aplicarRegrasSazonais(dateRef);

        List<ComissaoCalculadaFinal> results = finalRepository.findAll();
        assertEquals(1, results.size());
        
        ComissaoCalculadaFinal result = results.get(0);
        // Base = 5000. Bonus = 500. Total = 5500.
        assertEquals(0, new BigDecimal("5500.00").compareTo(result.getValorComissaoFinal()));
    }

    @Test
    @DisplayName("Cenário 2: deveAplicarBonusNaBaseDeVendasParaMatric227EmSetembro")
    void deveAplicarBonusNaBaseDeVendasParaMatric227EmSetembro() {
        LocalDate dateRef = LocalDate.of(2025, 9, 1); // 30 dias
        String matricula = "MATRIC-227";

        // Vendas Totais = 100.000. Comissao 5% = 5.000. Trabalhou mes inteiro.
        setupData(dateRef, matricula, 200, 10, new BigDecimal("100000.00"), new BigDecimal("0.05"), new BigDecimal("5000.00"));

        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.BONUS_BASE);
        regra.setMesCompetencia("2025-09");
        regra.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra.setCondicoesAplicacao("{\"matricula\": \"MATRIC-227\"}");
        regra.setValorModificador(new BigDecimal("20000.00"));
        regra.setDescricaoRegra("Bonus Base");
        regra.setCriadoPor("SISTEMA");
        regraRepository.save(regra);

        service.aplicarRegrasSazonais(dateRef);

        List<ComissaoCalculadaFinal> results = finalRepository.findAll();
        assertEquals(1, results.size());

        ComissaoCalculadaFinal result = results.get(0);
        // A regra é: (Vendas + 20000) * Percentual
        // Vendas Totais + 20000 = 120000
        // 120000 * 5% = 6000
        assertEquals(0, new BigDecimal("6000.00").compareTo(result.getValorComissaoFinal()));
    }

    @Test
    @DisplayName("Cenário 3: deveFazerOverrideDePercentualParaCargo300EmAgosto")
    void deveFazerOverrideDePercentualParaCargo300EmAgosto() {
        LocalDate dateRef = LocalDate.of(2025, 8, 1);
        String matricula = "MATRIC-999";

        // Vendas Totais = 100.000. Percentual Antigo = 1% (0.01). Comissao antiga = 1.000.
        setupData(dateRef, matricula, 300, 10, new BigDecimal("100000.00"), new BigDecimal("0.01"), new BigDecimal("1000.00"));

        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.OVERRIDE_PERCENTUAL);
        regra.setMesCompetencia("2025-08");
        regra.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra.setCondicoesAplicacao("{\"codMarca\": 10, \"codCargo\": 300}");
        regra.setValorModificador(new BigDecimal("0.0175")); // Novo multiplicador: 1.75%
        regra.setDescricaoRegra("Override Percentual");
        regra.setCriadoPor("SISTEMA");
        regraRepository.save(regra);

        service.aplicarRegrasSazonais(dateRef);

        List<ComissaoCalculadaFinal> results = finalRepository.findAll();
        assertEquals(1, results.size());

        ComissaoCalculadaFinal result = results.get(0);
        // Comissao = 100000 * 0.0175 = 1750
        assertEquals(0, new BigDecimal("1750.00").compareTo(result.getValorComissaoFinal()));
    }

    @Test
    @DisplayName("Cenário 4: deveAplicarBonusBlackFridayEmNovembro (Vendedor vs Gerente)")
    void deveAplicarBonusBlackFridayEmNovembro() {
        LocalDate dateRef = LocalDate.of(2025, 11, 1); // Novembro = 30 dias

        // Vendedor Comum (Cargo 200)
        String matVendedor = "MAT-VEND";
        setupData(dateRef, matVendedor, 200, 10, new BigDecimal("60000.00"), new BigDecimal("0.05"), new BigDecimal("3000.00"));

        // Gerente (Cargo 150)
        String matGerente = "MAT-GER";
        setupData(dateRef, matGerente, 150, 10, new BigDecimal("60000.00"), new BigDecimal("0.05"), new BigDecimal("3000.00"));

        RegraNegocioDinamica regra = new RegraNegocioDinamica();
        regra.setTipoRegra(TipoRegra.BLACK_FRIDAY);
        regra.setMesCompetencia("2025-11");
        regra.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra.setCondicoesAplicacao("{}"); // Aplica a todos
        regra.setValorModificador(new BigDecimal("0")); // Valor ignorado pela logica especifica
        regra.setDescricaoRegra("Black Friday");
        regra.setCriadoPor("SISTEMA");
        regraRepository.save(regra);

        service.aplicarRegrasSazonais(dateRef);

        List<ComissaoCalculadaFinal> results = finalRepository.findAll();
        assertEquals(2, results.size());

        ComissaoCalculadaFinal resVendedor = results.stream().filter(r -> r.getMatricula().equals(matVendedor)).findFirst().get();
        ComissaoCalculadaFinal resGerente = results.stream().filter(r -> r.getMatricula().equals(matGerente)).findFirst().get();

        // Calculo da Base BF: Vendas (60k) / 30 * 7 = 14000.
        // Bonus Vendedor = 14000 * 1% (0.01) = 140. Total = 3000 + 140 = 3140.
        assertEquals(0, new BigDecimal("3140.00").compareTo(resVendedor.getValorComissaoFinal()));

        // Bonus Gerente = 14000 * 0.5% (0.005) = 70. Total = 3000 + 70 = 3070.
        assertEquals(0, new BigDecimal("3070.00").compareTo(resGerente.getValorComissaoFinal()));
    }

    @Test
    @DisplayName("Cenário 5: deveAplicarFaixaDeVendasEscalonadaEmDezembro")
    void deveAplicarFaixaDeVendasEscalonadaEmDezembro() {
        LocalDate dateRef = LocalDate.of(2025, 12, 1);
        String matricula = "MATRIC-500";

        // Vendas Totais = 55.000. Comissao 5% = 2.750
        setupData(dateRef, matricula, 200, 10, new BigDecimal("55000.00"), new BigDecimal("0.05"), new BigDecimal("2750.00"));

        // Regra > 40k = 3500
        RegraNegocioDinamica regra1 = new RegraNegocioDinamica();
        regra1.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra1.setMesCompetencia("2025-12");
        regra1.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra1.setCondicoesAplicacao("{\"minVendas\": 40000}");
        regra1.setValorModificador(new BigDecimal("3500.00"));
        regra1.setDescricaoRegra("Faixa 1");
        regra1.setCriadoPor("SISTEMA");
        regraRepository.save(regra1);

        // Regra > 50k = 4000
        RegraNegocioDinamica regra2 = new RegraNegocioDinamica();
        regra2.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra2.setMesCompetencia("2025-12");
        regra2.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra2.setCondicoesAplicacao("{\"minVendas\": 50000}");
        regra2.setValorModificador(new BigDecimal("4000.00"));
        regra2.setDescricaoRegra("Faixa 2");
        regra2.setCriadoPor("SISTEMA");
        regraRepository.save(regra2);

        // Regra > 60k = 4500
        RegraNegocioDinamica regra3 = new RegraNegocioDinamica();
        regra3.setTipoRegra(TipoRegra.FAIXA_VENDAS);
        regra3.setMesCompetencia("2025-12");
        regra3.setStatusAprovacao(StatusAprovacao.APROVADA);
        regra3.setCondicoesAplicacao("{\"minVendas\": 60000}");
        regra3.setValorModificador(new BigDecimal("4500.00"));
        regra3.setDescricaoRegra("Faixa 3");
        regra3.setCriadoPor("SISTEMA");
        regraRepository.save(regra3);

        service.aplicarRegrasSazonais(dateRef);

        List<ComissaoCalculadaFinal> results = finalRepository.findAll();
        assertEquals(1, results.size());

        ComissaoCalculadaFinal result = results.get(0);
        // Venda é 55000. Pega a regra de 50k (4000). Total = 2750 + 4000 = 6750.
        assertEquals(0, new BigDecimal("6750.00").compareTo(result.getValorComissaoFinal()));
    }

    private void setupData(LocalDate dateRef, String matricula, Integer codCargo, Integer codMarca, BigDecimal vendas, BigDecimal percentual, BigDecimal valorProporcional) {
        BaseRH rh = new BaseRH();
        rh.setDateRef(dateRef);
        rh.setMatricula(matricula);
        rh.setCodCargo(codCargo);
        rh.setCodMarca(codMarca);
        rh.setCodLoja(1);
        rh.setDescrLoja("LOJA 1");
        rh.setDescrMarca("MARCA");
        rh.setDescrCargo("CARGO");
        rh.setDataAdmissao(LocalDate.of(2020, 1, 1));
        rhRepository.save(rh);

        ComissaoCalculadaBase base = new ComissaoCalculadaBase();
        base.setDateRef(dateRef);
        base.setMatricula(matricula);
        base.setCodCargo(codCargo);
        base.setCodLoja(1);
        base.setValorBaseVendas(vendas);
        base.setPercentualAplicado(percentual);
        base.setValorComissaoGerado(valorProporcional); // O valor base
        baseRepository.save(base);

        ComissaoCalculadaProporcional prop = new ComissaoCalculadaProporcional();
        prop.setDateRef(dateRef);
        prop.setMatricula(matricula);
        prop.setIdComissaoBase(base.getId());
        prop.setCodLoja(1);
        prop.setDiasDoMes(dateRef.lengthOfMonth());
        prop.setDiasTrabalhados(dateRef.lengthOfMonth());
        prop.setValorComissaoProporcional(valorProporcional); // Assumindo q n houve faltas
        prop.setMotivoProporcionalidade("INTEGRAL");
        proporcionalRepository.save(prop);
    }
}
