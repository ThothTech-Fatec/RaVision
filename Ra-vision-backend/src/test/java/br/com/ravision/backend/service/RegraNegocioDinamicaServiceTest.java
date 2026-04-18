package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.RegraNegocioDinamica;
import br.com.ravision.backend.domain.StatusAprovacao;
import br.com.ravision.backend.domain.TipoRegra;
import br.com.ravision.backend.dto.RegraNegocioRequest;
import br.com.ravision.backend.dto.RegraNegocioResponse;
import br.com.ravision.backend.repository.RegraNegocioDinamicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegraNegocioDinamicaServiceTest {

    @Mock
    private RegraNegocioDinamicaRepository repository;

    @Mock
    private HistoricoService historicoService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private RegraNegocioDinamicaService service;

    private RegraNegocioRequest request;
    private RegraNegocioDinamica regraBase;

    @BeforeEach
    void setUp() {
        request = RegraNegocioRequest.builder()
                .descricaoRegra("Teste Regra")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2023-10")
                .condicoesAplicacao("condicao")
                .valorModificador(new BigDecimal("500.00"))
                .build();

        regraBase = RegraNegocioDinamica.builder()
                .id(1L)
                .descricaoRegra("Teste Regra")
                .tipoRegra(TipoRegra.BONUS_FIXO)
                .mesCompetencia("2023-10")
                .condicoesAplicacao("condicao")
                .valorModificador(new BigDecimal("500.00"))
                .statusAprovacao(StatusAprovacao.PENDENTE)
                .criadoPor("user123")
                .dataCriacao(LocalDateTime.now())
                .build();

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("user123");
    }

    @Test
    void criarRegra_DeveSalvarComStatusPendente() {
        when(repository.save(any(RegraNegocioDinamica.class))).thenReturn(regraBase);

        RegraNegocioResponse response = service.criarRegra(request);

        assertNotNull(response);
        assertEquals(StatusAprovacao.PENDENTE, response.getStatusAprovacao());
        assertEquals("user123", response.getCriadoPor());
        verify(repository, times(1)).save(any(RegraNegocioDinamica.class));
        verify(historicoService, times(1)).registrarAcao(eq("user123"), eq("Regra Criada"), anyString());
    }

    @Test
    void editarRegra_DeveVoltarParaStatusPendente() {
        regraBase.setStatusAprovacao(StatusAprovacao.APROVADA);
        when(repository.findById(1L)).thenReturn(Optional.of(regraBase));
        when(repository.save(any(RegraNegocioDinamica.class))).thenReturn(regraBase);

        RegraNegocioResponse response = service.editarRegra(1L, request);

        assertNotNull(response);
        assertEquals(StatusAprovacao.PENDENTE, response.getStatusAprovacao());
        verify(repository, times(1)).save(regraBase);
        verify(historicoService, times(1)).registrarAcao(eq("user123"), eq("Regra Editada"), anyString());
    }

    @Test
    void aprovarRegra_DeveMudarParaAprovada() {
        when(repository.findById(1L)).thenReturn(Optional.of(regraBase));
        when(repository.save(any(RegraNegocioDinamica.class))).thenReturn(regraBase);

        RegraNegocioResponse response = service.aprovarRegra(1L);

        assertNotNull(response);
        assertEquals(StatusAprovacao.APROVADA, response.getStatusAprovacao());
        verify(historicoService, times(1)).registrarAcao(eq("user123"), eq("Regra Aprovada"), anyString());
    }

    @Test
    void recusarRegra_DeveMudarParaRecusada() {
        when(repository.findById(1L)).thenReturn(Optional.of(regraBase));
        when(repository.save(any(RegraNegocioDinamica.class))).thenReturn(regraBase);

        RegraNegocioResponse response = service.recusarRegra(1L);

        assertNotNull(response);
        assertEquals(StatusAprovacao.RECUSADA, response.getStatusAprovacao());
        verify(historicoService, times(1)).registrarAcao(eq("user123"), eq("Regra Recusada"), anyString());
    }
}
