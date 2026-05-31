package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutivaKPIsDTO {
    private BigDecimal faturamentoAtual;
    private BigDecimal comissoesAtuais;
    private Double custoComissaoPercentual;
}
