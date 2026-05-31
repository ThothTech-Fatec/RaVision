package br.com.ravision.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoramentoIADTO {
    private LocalDateTime timestamp;
    private String perguntaUsuario;
    private String respostaIA;
    private Long tempoRespostaMs;
    private String ferramentaUtilizada;
    private Boolean sucessoFerramenta;
    private String usuarioLogado;
}
