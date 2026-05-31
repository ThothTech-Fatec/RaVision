package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.DashboardAgregacaoDTO;
import br.com.ravision.backend.dto.DashboardTotalDTO;
import br.com.ravision.backend.dto.ExecutivaKPIsDTO;
import br.com.ravision.backend.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void getComissoesPorLoja_deveRetornarStatusOkELista() throws Exception {
        LocalDate dateRef = LocalDate.of(2025, 11, 1);
        when(dashboardService.getComissoesPorLoja(dateRef))
                .thenReturn(Arrays.asList(new DashboardAgregacaoDTO("Loja A", new BigDecimal("1000.00"))));

        mockMvc.perform(get("/api/dashboards/comissoes/loja")
                        .param("dateRef", "2025-11-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chave").value("Loja A"))
                .andExpect(jsonPath("$[0].valor").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void getComissoesPorMarca_deveRetornarStatusOkELista() throws Exception {
        LocalDate dateRef = LocalDate.of(2025, 10, 1);
        when(dashboardService.getComissoesPorMarca(dateRef))
                .thenReturn(Arrays.asList(new DashboardAgregacaoDTO("Marca B", new BigDecimal("5000.00"))));

        mockMvc.perform(get("/api/dashboards/comissoes/marca")
                        .param("dateRef", "2025-10-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chave").value("Marca B"))
                .andExpect(jsonPath("$[0].valor").value(5000.00));
    }

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void getTotalComissaoGeral_deveRetornarStatusOkETotal() throws Exception {
        LocalDate dateRef = LocalDate.of(2025, 9, 1);
        when(dashboardService.getTotalComissaoGeral(dateRef))
                .thenReturn(new DashboardTotalDTO(new BigDecimal("150000.00")));

        mockMvc.perform(get("/api/dashboards/comissoes/geral")
                        .param("dateRef", "2025-09-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150000.00));
    }

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void getKPIsExecutiva_deveRetornarStatusOk() throws Exception {
        LocalDate dateRef = LocalDate.of(2025, 9, 1);
        when(dashboardService.getKPIsExecutiva(dateRef))
                .thenReturn(new ExecutivaKPIsDTO(new BigDecimal("100000.00"), new BigDecimal("5000.00"), 5.0));

        mockMvc.perform(get("/api/dashboards/comissoes/executiva/kpis")
                        .param("dateRef", "2025-09-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faturamentoAtual").value(100000.00))
                .andExpect(jsonPath("$.comissoesAtuais").value(5000.00))
                .andExpect(jsonPath("$.custoComissaoPercentual").value(5.0));
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void endpointsDashboard_devemRetornarForbiddenParaVendedor() throws Exception {
        mockMvc.perform(get("/api/dashboards/comissoes/geral").param("dateRef", "2025-09-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointsDashboard_devemRetornarUnauthorizedSemLogin() throws Exception {
        mockMvc.perform(get("/api/dashboards/comissoes/loja").param("dateRef", "2025-09-01"))
                .andExpect(status().isForbidden());
    }
}
