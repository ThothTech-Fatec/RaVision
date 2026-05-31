package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoEvolucaoDTO {
    private LocalDate mes;
    private BigDecimal faturamento;
    private BigDecimal comissoes;
}
