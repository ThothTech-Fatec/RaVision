package br.com.ravision.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoramentoAgregadoDTO {
    private Long totalRequisicoes;
    private Double tempoMedioGeralMs;
    private List<EstatisticaDiariaDTO> estatisticasDiarias;
    private List<MonitoramentoIADTO> logsRecentes;
}
