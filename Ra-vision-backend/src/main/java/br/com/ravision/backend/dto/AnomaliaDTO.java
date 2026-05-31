package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.StatusAnaliseAnomalia;
import br.com.ravision.backend.domain.TipoAnomalia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomaliaDTO {
    private Long id;
    private LocalDate dateRef;
    private String matricula;
    private Integer codCargo;
    private BigDecimal valorComissao;
    private BigDecimal limiteEsperado;
    private BigDecimal valorMedio;
    private TipoAnomalia tipoAnomalia;
    private StatusAnaliseAnomalia statusAnalise;
    private String justificativa;
}
