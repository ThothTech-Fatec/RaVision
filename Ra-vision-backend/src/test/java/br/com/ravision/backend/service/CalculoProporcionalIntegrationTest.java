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
@Transactional // Garante que o H2 faça rollback ao final de cada teste
public class CalculoProporcionalIntegrationTest {

    @Autowired private CalculoProporcionalService service;
    @Autowired private ComissaoCalculadaBaseRepository baseRepository;
    @Autowired private BaseRHRepository rhRepository;
    @Autowired private IntercorrenciaRHRepository intercorrenciaRepository;
    @Autowired private ComissaoCalculadaProporcionalRepository proporcionalRepository;

    @BeforeEach
    void cleanUp() {
        // Limpar o banco H2 antes de cada inserção de dados mockados
        baseRepository.deleteAll();
        rhRepository.deleteAll();
        intercorrenciaRepository.deleteAll();
        proporcionalRepository.deleteAll();
    }

    @Test
    @DisplayName("Cenário 1: deveCalcularRateioDeMultiplasLojasParaMatric293 (Julho/2025)")
    void deveCalcularRateioDeMultiplasLojasParaMatric293() {
        LocalDate dateRef = LocalDate.of(2025, 7, 1); // Julho = 31 dias
        String matricula = "MATRIC-293";

        // Setup RH
        BaseRH rh = new BaseRH();
        rh.setDateRef(dateRef);
        rh.setMatricula(matricula);
        rh.setCodLoja(1);
        rh.setDescrLoja("LOJA ORIGINAL");
        rh.setCodMarca(10);
        rh.setDescrMarca("MARCA 10");
        rh.setDataAdmissao(LocalDate.of(2020, 1, 1));
        rh.setCodCargo(100);
        rh.setDescrCargo("GERENTE");
        rhRepository.save(rh);

        // Setup Comissao Base (Total: 6200.00)
        ComissaoCalculadaBase base = new ComissaoCalculadaBase();
        base.setDateRef(dateRef);
        base.setMatricula(matricula);
        base.setCodLoja(1); // Loja original
        base.setCodCargo(100);
        base.setValorBaseVendas(new BigDecimal("200000.00"));
        base.setPercentualAplicado(new BigDecimal("0.031"));
        base.setValorComissaoGerado(new BigDecimal("6200.00"));
        baseRepository.save(base);

        // Setup Intercorrencia: 10 dias na LOJA-5
        IntercorrenciaRH transf = new IntercorrenciaRH();
        transf.setMatricula(matricula);
        transf.setTipo("TRANSFERENCIA");
        transf.setDataInicio(LocalDate.of(2025, 7, 1));
        transf.setDataFim(LocalDate.of(2025, 7, 10)); // 10 dias
        transf.setCodLojaSecundaria(5);
        intercorrenciaRepository.save(transf);

        // Execucao
        service.calcularProporcional(dateRef);

        // Verificacao
        List<ComissaoCalculadaProporcional> results = proporcionalRepository.findAll();
        assertEquals(2, results.size(), "O sistema deve ter gerado duas linhas rateando o valor");

        ComissaoCalculadaProporcional lojaOriginal = results.stream().filter(r -> r.getCodLoja() == 1).findFirst().get();
        ComissaoCalculadaProporcional loja5 = results.stream().filter(r -> r.getCodLoja() == 5).findFirst().get();

        assertEquals(21, lojaOriginal.getDiasTrabalhados());
        assertEquals(10, loja5.getDiasTrabalhados());

        // 6200 / 31 = 200 por dia. Loja 5 = 2000. Loja 1 = 4200.
        assertEquals(0, new BigDecimal("4200.00").compareTo(lojaOriginal.getValorComissaoProporcional()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(loja5.getValorComissaoProporcional()));
    }

    @Test
    @DisplayName("Cenário 2: deveCalcularAfastamentoMenorQue15DiasAplicandoPisoParaMatric113 (Agosto/2025)")
    void deveCalcularAfastamentoMenorQue15DiasAplicandoPisoParaMatric113() {
        LocalDate dateRef = LocalDate.of(2025, 8, 1); // Agosto = 31 dias
        String matricula = "MATRIC-113";

        // Setup RH
        BaseRH rh = new BaseRH();
        rh.setDateRef(dateRef);
        rh.setMatricula(matricula);
        rh.setCodLoja(10);
        rh.setDescrLoja("LOJA 10");
        rh.setCodMarca(20);
        rh.setDescrMarca("MARCA 20");
        rh.setDataAdmissao(LocalDate.of(2020, 1, 1));
        rh.setCodCargo(200);
        rh.setDescrCargo("VENDEDOR");
        rhRepository.save(rh);

        // Setup Comissao Base com vendas super baixas para forcar o Piso de 3500
        ComissaoCalculadaBase base = new ComissaoCalculadaBase();
        base.setDateRef(dateRef);
        base.setMatricula(matricula);
        base.setCodLoja(10);
        base.setCodCargo(200);
        base.setValorBaseVendas(new BigDecimal("11500.00")); // Vendas do Mês
        base.setPercentualAplicado(new BigDecimal("0.05"));
        base.setValorComissaoGerado(new BigDecimal("575.00")); // Comissao Vendas Reais
        baseRepository.save(base);

        // Setup Intercorrencia: Afastamento 8 dias (04/08 a 11/08)
        IntercorrenciaRH atestado = new IntercorrenciaRH();
        atestado.setMatricula(matricula);
        atestado.setTipo("ATESTADO");
        atestado.setDataInicio(LocalDate.of(2025, 8, 4));
        atestado.setDataFim(LocalDate.of(2025, 8, 11)); // 8 dias. Dias Trabalhados = 23.
        intercorrenciaRepository.save(atestado);

        // Execucao
        service.calcularProporcional(dateRef);

        // Verificacao
        List<ComissaoCalculadaProporcional> results = proporcionalRepository.findAll();
        assertEquals(1, results.size());
        
        ComissaoCalculadaProporcional result = results.get(0);
        assertEquals(23, result.getDiasTrabalhados());
        assertEquals("AFASTAMENTO", result.getMotivoProporcionalidade());

        // Calculo Interno Realizado:
        // Media Diaria Vendas = 11500 / 23 = 500
        // Base Adicional = 500 * 8 = 4000
        // Comissao Adicional = 4000 * 5% = 200
        // Total = 575 + 200 = 775. 
        // 775 < 3500 (PISO) -> DEVE PAGAR 3500.
        assertEquals(0, new BigDecimal("3500.00").compareTo(result.getValorComissaoProporcional()));
    }

    @Test
    @DisplayName("Cenário 3: deveCalcularAfastamentoMaiorQue15DiasLimitandoParaMatric115 (Agosto/2025)")
    void deveCalcularAfastamentoMaiorQue15DiasLimitandoParaMatric115() {
        LocalDate dateRef = LocalDate.of(2025, 8, 1); // Agosto = 31 dias
        String matricula = "MATRIC-115";

        // Setup RH
        BaseRH rh = new BaseRH();
        rh.setDateRef(dateRef);
        rh.setMatricula(matricula);
        rh.setCodLoja(10);
        rh.setDescrLoja("LOJA 10");
        rh.setCodMarca(20);
        rh.setDescrMarca("MARCA 20");
        rh.setDataAdmissao(LocalDate.of(2020, 1, 1));
        rh.setCodCargo(200);
        rh.setDescrCargo("VENDEDOR");
        rhRepository.save(rh);

        // Setup Comissao Base (Altas Vendas)
        ComissaoCalculadaBase base = new ComissaoCalculadaBase();
        base.setDateRef(dateRef);
        base.setMatricula(matricula);
        base.setCodLoja(10);
        base.setCodCargo(200);
        base.setValorBaseVendas(new BigDecimal("90000.00")); // Vendas do Mes Real
        base.setPercentualAplicado(new BigDecimal("0.05"));
        base.setValorComissaoGerado(new BigDecimal("4500.00")); // Comissao Real
        baseRepository.save(base);

        // Setup Intercorrencia: Afastamento 22 dias (01/08 a 22/08)
        IntercorrenciaRH atestado = new IntercorrenciaRH();
        atestado.setMatricula(matricula);
        atestado.setTipo("ATESTADO");
        atestado.setDataInicio(LocalDate.of(2025, 8, 1));
        atestado.setDataFim(LocalDate.of(2025, 8, 22)); // 22 dias. Dias Trabalhados = 9.
        intercorrenciaRepository.save(atestado);

        // Execucao
        service.calcularProporcional(dateRef);

        // Verificacao
        List<ComissaoCalculadaProporcional> results = proporcionalRepository.findAll();
        assertEquals(1, results.size());
        
        ComissaoCalculadaProporcional result = results.get(0);
        assertEquals(9, result.getDiasTrabalhados());
        
        // Calculo Interno:
        // Media Diaria Vendas = 90000 / 9 = 10000
        // Base Adicional (Limitada a 15 dias!!) = 10000 * 15 = 150000
        // Comissao Adicional = 150000 * 5% = 7500
        // Total = 4500 + 7500 = 12000. (7 dias foram descartados).
        // 12000 > 3500 -> Mantem os 12000.
        assertEquals(0, new BigDecimal("12000.00").compareTo(result.getValorComissaoProporcional()));
    }

    @Test
    @DisplayName("Cenário 4: deveGarantirIsencaoDePerdasParaMaternidadeDaMatric71 (Outubro/2025)")
    void deveGarantirIsencaoDePerdasParaMaternidadeDaMatric71() {
        LocalDate dateRef = LocalDate.of(2025, 10, 1); // Outubro = 31 dias
        String matricula = "MATRIC-71";

        // Setup RH
        BaseRH rh = new BaseRH();
        rh.setDateRef(dateRef);
        rh.setMatricula(matricula);
        rh.setCodLoja(10);
        rh.setDescrLoja("LOJA 10");
        rh.setCodMarca(20);
        rh.setDescrMarca("MARCA 20");
        rh.setDataAdmissao(LocalDate.of(2020, 1, 1));
        rh.setCodCargo(200);
        rh.setDescrCargo("VENDEDORA");
        rhRepository.save(rh);

        // Setup Comissao Base
        ComissaoCalculadaBase base = new ComissaoCalculadaBase();
        base.setDateRef(dateRef);
        base.setMatricula(matricula);
        base.setCodLoja(10);
        base.setCodCargo(200);
        base.setValorBaseVendas(new BigDecimal("150000.00"));
        base.setPercentualAplicado(new BigDecimal("0.05"));
        base.setValorComissaoGerado(new BigDecimal("7500.00"));
        baseRepository.save(base);

        // Setup Intercorrencia: Maternidade a partir do dia 1 ate fim do mes (e continua...)
        IntercorrenciaRH maternidade = new IntercorrenciaRH();
        maternidade.setMatricula(matricula);
        maternidade.setTipo("LICENCA_MATERNIDADE");
        maternidade.setDataInicio(LocalDate.of(2025, 10, 1));
        maternidade.setDataFim(LocalDate.of(2026, 3, 30));
        intercorrenciaRepository.save(maternidade);

        // Execucao
        service.calcularProporcional(dateRef);

        // Verificacao
        List<ComissaoCalculadaProporcional> results = proporcionalRepository.findAll();
        assertEquals(1, results.size());
        
        ComissaoCalculadaProporcional result = results.get(0);
        assertEquals("LICENCA_MATERNIDADE", result.getMotivoProporcionalidade());
        
        // Assertivas da Regra: Sem desconto e considerou dias trabalhados integrais
        assertEquals(31, result.getDiasTrabalhados(), "Os dias trabalhados devem ser iguais aos dias do mês para não haver cortes");
        assertEquals(0, new BigDecimal("7500.00").compareTo(result.getValorComissaoProporcional()));
    }
}
