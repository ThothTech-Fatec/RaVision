package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacaoResponseDTO {

    private Long regraId;
    private BigDecimal custoTotalAtual;
    private BigDecimal custoTotalSimulado;
    private BigDecimal impactoFinanceiro;
    private Integer quantidadeFuncionariosAfetados;
}
