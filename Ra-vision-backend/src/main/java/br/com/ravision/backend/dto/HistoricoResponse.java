package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoResponse {
    private Long id;
    private String username;
    private String acaoRealizada;
    private LocalDateTime dataHora;
    private String detalhesAcao;
}
