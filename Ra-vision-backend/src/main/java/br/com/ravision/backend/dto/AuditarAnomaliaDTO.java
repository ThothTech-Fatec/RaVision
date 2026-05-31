package br.com.ravision.backend.dto;

import br.com.ravision.backend.domain.StatusAnaliseAnomalia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditarAnomaliaDTO {
    private StatusAnaliseAnomalia statusAnalise;
    private String justificativa;
}
