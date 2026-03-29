package br.com.ravision.backend.service;

import br.com.ravision.backend.repository.BaseComissionamentoRepository;
import br.com.ravision.backend.repository.BaseRHRepository;
import br.com.ravision.backend.repository.BaseVendasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @InjectMocks
    private UploadService uploadService;

    @Mock
    private BaseRHRepository rhRepository;

    @Mock
    private BaseVendasRepository vendasRepository;

    @Mock
    private BaseComissionamentoRepository comissaoRepository;

    private MockMultipartFile fileRH;
    private MockMultipartFile fileVendas;
    private MockMultipartFile fileComissao;

    @BeforeEach
    void setUp() {
        String csvRH = "date_ref;cod_marca;descr_marca;cod_loja;descr_loja;matricula;data_admiss;data_demiss;cod_cargo;descr_cargo\n" +
                       "01/01/2024;1;MarcaA;10;LojaX;MAT-1;01/01/2020;;100;VENDEDOR\n";
        fileRH = new MockMultipartFile("rh", "rh.csv", "text/csv", csvRH.getBytes(StandardCharsets.UTF_8));

        String csvVendas = "date_ref;cod_marca;descr_marca;cod_loja;descr_loja;matricula;vlr_venda\n" +
                           "01/01/2024;1;MarcaA;10;LojaX;MAT-1;1500.50\n";
        fileVendas = new MockMultipartFile("vendas", "vendas.csv", "text/csv", csvVendas.getBytes(StandardCharsets.UTF_8));

        String csvComissao = "cod_marca;descr_marca;cod_cargo;descr_cargo;pct_comiss\n" +
                             "1;MarcaA;100;VENDEDOR;5.5\n";
        fileComissao = new MockMultipartFile("comissao", "comissao.csv", "text/csv", csvComissao.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void processarLote_sucessoQuandoDatasBatem() {
        assertDoesNotThrow(() -> uploadService.processarLote(fileRH, fileVendas, fileComissao));
        verify(rhRepository, times(1)).saveAll(any());
        verify(vendasRepository, times(1)).saveAll(any());
        verify(comissaoRepository, times(1)).saveAll(any());
    }

    @Test
    void processarLote_falhaQuandoDatasDivergem() {
        String csvVendasDivergente = "date_ref;cod_marca;descr_marca;cod_loja;descr_loja;matricula;vlr_venda\n" +
                                     "01/02/2024;1;MarcaA;10;LojaX;MAT-1;1500.50\n";
        MockMultipartFile fileVendasDivergente = new MockMultipartFile(
                "vendas", "vendas_dif.csv", "text/csv", csvVendasDivergente.getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                uploadService.processarLote(fileRH, fileVendasDivergente, fileComissao));

        assertTrue(exception.getMessage().contains("As datas de referência (Competência) da base de RH e Vendas divergem"));
        verify(rhRepository, never()).saveAll(any());
    }

    @Test
    void processarLote_falhaQuandoPlanilhaVazia() {
        MockMultipartFile fileVazio = new MockMultipartFile("rh", "rh.csv", "text/csv", "".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () ->
                uploadService.processarLote(fileRH, fileVazio, fileComissao));
    }
}
