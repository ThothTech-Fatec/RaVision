package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioFolhaDTO {
    private String matricula;
    private String loja;
    private Integer diasTrabalhados;
    private Integer diasDoMes;
    private String motivo;
    private BigDecimal valorComissao;
}
