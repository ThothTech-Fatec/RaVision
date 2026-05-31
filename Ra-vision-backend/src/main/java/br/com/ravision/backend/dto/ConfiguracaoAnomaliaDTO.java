package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.TipoAlgoritmo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoAnomaliaDTO {
    private TipoAlgoritmo algoritmo;
    private Double limiteMultiplicador;
    private Boolean agruparPorCargo;
}
