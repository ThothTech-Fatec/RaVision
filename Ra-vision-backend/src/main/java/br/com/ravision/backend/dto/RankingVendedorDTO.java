package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingVendedorDTO {
    private String matricula;
    private String nome;
    private String loja;
    private BigDecimal vendas;
    private BigDecimal comissao;
}
