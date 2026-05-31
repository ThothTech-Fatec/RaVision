package br.com.ravision.backend.controller;

import br.com.ravision.backend.dto.MonitoramentoAgregadoDTO;
import br.com.ravision.backend.dto.MonitoramentoIADTO;
import br.com.ravision.backend.service.MonitoramentoIAService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MonitoramentoIAControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MonitoramentoIAService monitoramentoIAService;

    @Test
    void ingerirMetricas_deveRetornarAcceptedComApiKeyValida() throws Exception {
        MonitoramentoIADTO dto = new MonitoramentoIADTO();
        dto.setPerguntaUsuario("Teste");
        dto.setTempoRespostaMs(100L);

        mockMvc.perform(post("/api/monitoramento/ia")
                        .header("X-API-Key", "RAVISION_SECURE_API_KEY_2026")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        verify(monitoramentoIAService).salvarMetrica(any());
    }

    @Test
    void ingerirMetricas_deveRetornarUnauthorizedSemApiKey() throws Exception {
        MonitoramentoIADTO dto = new MonitoramentoIADTO();
        dto.setPerguntaUsuario("Teste");

        mockMvc.perform(post("/api/monitoramento/ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CTO")
    void buscarEstatisticas_deveRetornarDadosParaCTO() throws Exception {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = LocalDate.of(2026, 5, 31);

        MonitoramentoAgregadoDTO mockDto = new MonitoramentoAgregadoDTO(10L, 500.0, Collections.emptyList(), Collections.emptyList());
        when(monitoramentoIAService.buscarMetricasAgregadas(start, end)).thenReturn(mockDto);

        mockMvc.perform(get("/api/monitoramento/ia")
                        .param("dataInicio", "2026-05-01")
                        .param("dataFim", "2026-05-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequisicoes").value(10));
    }

    @Test
    @WithMockUser(roles = "VENDEDOR")
    void buscarEstatisticas_deveRetornarForbiddenParaVendedor() throws Exception {
        mockMvc.perform(get("/api/monitoramento/ia")
                        .param("dataInicio", "2026-05-01")
                        .param("dataFim", "2026-05-31"))
                .andExpect(status().isForbidden());
    }
}
