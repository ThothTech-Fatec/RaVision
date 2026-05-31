package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.StatusErro;
import br.com.ravision.backend.domain.TipoErro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErroImportacaoDTO {
    private Long id;
    private LocalDateTime timestamp;
    private String nomeArquivo;
    private Integer linha;
    private String coluna;
    private String mensagemErro;
    private TipoErro tipoErro;
    private StatusErro status;
    private String usuarioUpload;
}
