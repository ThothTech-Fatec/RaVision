package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.TipoRegra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegraNegocioRequest {

    @NotBlank(message = "A descrição da regra é obrigatória")
    private String descricaoRegra;

    @NotNull(message = "O tipo da regra é obrigatório")
    private TipoRegra tipoRegra;

    @NotBlank(message = "O mês de competência é obrigatório")
    private String mesCompetencia;

    private String condicoesAplicacao;

    @NotNull(message = "O valor modificador é obrigatório")
    private BigDecimal valorModificador;
}
