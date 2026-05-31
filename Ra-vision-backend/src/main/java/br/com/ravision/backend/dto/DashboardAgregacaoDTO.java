package br.com.ravision.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAgregacaoDTO {
    private String chave; // Pode ser nome da loja, marca, etc.
    private BigDecimal valor; // Total da comissão agregada
}
