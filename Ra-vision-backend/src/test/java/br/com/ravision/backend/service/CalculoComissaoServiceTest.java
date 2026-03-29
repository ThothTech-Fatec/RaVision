package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.BaseComissionamento;
import br.com.ravision.backend.domain.BaseRH;
import br.com.ravision.backend.domain.BaseVendas;
import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import br.com.ravision.backend.repository.BaseComissionamentoRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import br.com.ravision.backend.repository.ComissaoCalculadaBaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CalculoComissaoServiceTest {

    @Mock
    private BaseRHRepository rhRepository;
    @Mock
    private BaseVendasRepository vendasRepository;
    @Mock
    private BaseComissionamentoRepository comissaoRepository;
    @Mock
    private ComissaoCalculadaBaseRepository calculoRepository;

    @InjectMocks
    private CalculoComissaoService service;

    private LocalDate dateRef = LocalDate.of(2025, 12, 1);

    @Test
    void garanteCalculoEspecificoPorCargo() {
        // Setup Mocks de RH
        BaseRH rhVendedor = new BaseRH();
        rhVendedor.setMatricula("V1");
        rhVendedor.setCodLoja(10);
        rhVendedor.setCodCargo(100);
        rhVendedor.setCodMarca(1);

        BaseRH rhGerente = new BaseRH();
        rhGerente.setMatricula("G1");
        rhGerente.setCodLoja(10);
        rhGerente.setCodCargo(150);
        rhGerente.setCodMarca(1);

        when(rhRepository.findByDateRef(dateRef)).thenReturn(Arrays.asList(rhVendedor, rhGerente));

        // Setup Mocks de Vendas
        BaseVendas venda1 = new BaseVendas();
        venda1.setMatricula("V1");
        venda1.setCodLoja(10);
        venda1.setVlrVenda(new BigDecimal("1000.00"));

        BaseVendas venda2 = new BaseVendas();
        venda2.setMatricula("G1"); 
        venda2.setCodLoja(10);
        venda2.setVlrVenda(new BigDecimal("500.00"));
        // Total da Loja 10 sera: 1000 + 500 = 1500

        when(vendasRepository.findByDateRef(dateRef)).thenReturn(Arrays.asList(venda1, venda2));

        // Setup Comissoes Fixas
        BaseComissionamento cVendedor = new BaseComissionamento();
        cVendedor.setCodMarca(1);
        cVendedor.setCodCargo(100);
        cVendedor.setPercentualComissao(new BigDecimal("2.00")); // 2%

        BaseComissionamento cGerente = new BaseComissionamento();
        cGerente.setCodMarca(1);
        cGerente.setCodCargo(150);
        cGerente.setPercentualComissao(new BigDecimal("1.50")); // 1.5%

        when(comissaoRepository.findAll()).thenReturn(Arrays.asList(cVendedor, cGerente));

        // Executar Motor
        service.calcularCompetencia(dateRef);

        // Capturar Resultados via Mockito
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ComissaoCalculadaBase>> captor = ArgumentCaptor.forClass(List.class);
        verify(calculoRepository).saveAll(captor.capture());

        List<ComissaoCalculadaBase> salvos = captor.getValue();
        assertEquals(2, salvos.size());

        ComissaoCalculadaBase resultVendedor = salvos.stream().filter(c -> c.getMatricula().equals("V1")).findFirst().get();
        ComissaoCalculadaBase resultGerente = salvos.stream().filter(c -> c.getMatricula().equals("G1")).findFirst().get();

        // VALIDACOES DoD
        // 1. Vendedor (Nao 150): Deve aplicar 2% em cima da venda de V1 (1000.00) = 20.00
        assertEquals(new BigDecimal("1000.00"), resultVendedor.getValorBaseVendas());
        assertEquals(new BigDecimal("2.00"), resultVendedor.getPercentualAplicado());
        assertEquals(0, new BigDecimal("20.00").compareTo(resultVendedor.getValorComissaoGerado()));

        // 2. Gerente (Cargo 150): Deve aplicar 1.5% em cima do Total da Loja (1500.00) = 22.50
        assertEquals(new BigDecimal("1500.00"), resultGerente.getValorBaseVendas());
        assertEquals(new BigDecimal("1.50"), resultGerente.getPercentualAplicado());
        assertEquals(0, new BigDecimal("22.50").compareTo(resultGerente.getValorComissaoGerado()));
    }
}
