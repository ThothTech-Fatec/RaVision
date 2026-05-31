package br.com.ravision.backend.service;

import br.com.ravision.backend.domain.ErroImportacao;
import br.com.ravision.backend.domain.StatusErro;
import br.com.ravision.backend.domain.TipoErro;
import br.com.ravision.backend.dto.ErroImportacaoDTO;
import br.com.ravision.backend.repository.ErroImportacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErroImportacaoServiceTest {

    @Mock
    private ErroImportacaoRepository repository;

    @InjectMocks
    private ErroImportacaoService service;

    private ErroImportacao erroPendente;
    private ErroImportacaoDTO dto;

    @BeforeEach
    void setUp() {
        erroPendente = new ErroImportacao();
        erroPendente.setId(1L);
        erroPendente.setTimestamp(LocalDateTime.now());
        erroPendente.setNomeArquivo("rh.csv");
        erroPendente.setLinha(10);
        erroPendente.setMensagemErro("Matrícula ausente");
        erroPendente.setTipoErro(TipoErro.MISSING_DATA);
        erroPendente.setStatus(StatusErro.PENDENTE);
        erroPendente.setUsuarioUpload("admin");

        dto = ErroImportacaoDTO.builder()
                .timestamp(erroPendente.getTimestamp())
                .nomeArquivo("rh.csv")
                .linha(10)
                .mensagemErro("Matrícula ausente")
                .tipoErro(TipoErro.MISSING_DATA)
                .usuarioUpload("admin")
                .build();
    }

    @Test
    void deveSalvarErroCorretamente() {
        when(repository.save(any(ErroImportacao.class))).thenReturn(erroPendente);

        ErroImportacaoDTO result = service.salvarErro(dto);

        assertNotNull(result);
        assertEquals(StatusErro.PENDENTE, result.getStatus());
        assertEquals("rh.csv", result.getNomeArquivo());
        verify(repository, times(1)).save(any(ErroImportacao.class));
    }

    @Test
    void deveBuscarErrosPaginados() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ErroImportacao> page = new PageImpl<>(List.of(erroPendente));
        when(repository.findByFiltros(any(), any(), any(), any(), eq(pageRequest))).thenReturn(page);

        Page<ErroImportacaoDTO> result = service.listarErros(null, null, null, null, pageRequest);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void deveMarcarComoResolvido() {
        when(repository.findById(1L)).thenReturn(Optional.of(erroPendente));
        when(repository.save(any(ErroImportacao.class))).thenReturn(erroPendente);

        ErroImportacaoDTO result = service.marcarComoResolvido(1L);

        assertEquals(StatusErro.RESOLVIDO, erroPendente.getStatus());
        assertEquals(StatusErro.RESOLVIDO, result.getStatus());
        verify(repository, times(1)).save(erroPendente);
    }
}
