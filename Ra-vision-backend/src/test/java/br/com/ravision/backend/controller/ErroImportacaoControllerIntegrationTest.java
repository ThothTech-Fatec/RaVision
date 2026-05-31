package br.com.ravision.backend.controller;

import br.com.ravision.backend.domain.ErroImportacao;
import br.com.ravision.backend.domain.StatusErro;
import br.com.ravision.backend.domain.TipoErro;
import br.com.ravision.backend.repository.ErroImportacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErroImportacaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ErroImportacaoRepository repository;

    private ErroImportacao erro;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        erro = new ErroImportacao();
        erro.setTimestamp(LocalDateTime.now());
        erro.setNomeArquivo("vendas.csv");
        erro.setLinha(5);
        erro.setMensagemErro("Valor inválido");
        erro.setTipoErro(TipoErro.INVALID_VALUE);
        erro.setStatus(StatusErro.PENDENTE);
        erro.setUsuarioUpload("admin");

        erro = repository.save(erro);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveListarErrosPaginados() throws Exception {
        mockMvc.perform(get("/api/erros-importacao")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nomeArquivo").value("vendas.csv"))
                .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveMarcarErroComoResolvido() throws Exception {
        mockMvc.perform(put("/api/erros-importacao/" + erro.getId() + "/resolver")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDO"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void deveContarErrosPendentes() throws Exception {
        mockMvc.perform(get("/api/erros-importacao/pendentes/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @WithMockUser(roles = "USUARIO_COMUM")
    void naoDeveAcessarSendoUsuarioComum() throws Exception {
        mockMvc.perform(get("/api/erros-importacao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
