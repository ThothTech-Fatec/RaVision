package br.com.ravision.backend.controller;


import br.com.ravision.backend.domain.TipoAlgoritmo;
import br.com.ravision.backend.dto.ConfiguracaoAnomaliaDTO;
import br.com.ravision.backend.repository.AnomaliaRepository;
import br.com.ravision.backend.repository.ConfiguracaoAnomaliaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnomaliaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfiguracaoAnomaliaRepository configuracaoRepository;

    @Autowired
    private AnomaliaRepository anomaliaRepository;

    @BeforeEach
    void setUp() {
        anomaliaRepository.deleteAll();
        configuracaoRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void testSalvarEBuscarConfiguracao() throws Exception {
        ConfiguracaoAnomaliaDTO dto = ConfiguracaoAnomaliaDTO.builder()
                .algoritmo(TipoAlgoritmo.Z_SCORE)
                .limiteMultiplicador(2.5)
                .agruparPorCargo(false)
                .build();

        // Testa POST
        mockMvc.perform(post("/api/anomalias/configuracao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algoritmo").value("Z_SCORE"))
                .andExpect(jsonPath("$.limiteMultiplicador").value(2.5));

        // Verifica no banco
        assertEquals(1, configuracaoRepository.count());

        // Testa GET
        mockMvc.perform(get("/api/anomalias/configuracao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algoritmo").value("Z_SCORE"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void testDispararDeteccaoComFalhaPorFaltaDeDados() throws Exception {
        // Dispara deteccao para um mes sem dados, deve retornar 400 Bad Request
        mockMvc.perform(post("/api/anomalias/disparar")
                .param("dateRef", "2026-05-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @WithMockUser(roles = "GESTOR_RH")
    void testBuscarAnomaliasComPaginacao() throws Exception {
        // Tenta buscar as anomalias, inicialmente vazio
        mockMvc.perform(get("/api/anomalias")
                .param("dateRef", "2026-05-01")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
