package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.StatusAprovacao;
import br.com.ravision.backend.domain.TipoRegra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegraNegocioResponse {

    private Long id;
    private String descricaoRegra;
    private TipoRegra tipoRegra;
    private String mesCompetencia;
    private String condicoesAplicacao;
    private BigDecimal valorModificador;
    private StatusAprovacao statusAprovacao;
    private String criadoPor;
    private LocalDateTime dataCriacao;
}
