package br.com.ravision.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticaDiariaDTO {
    private LocalDate data;
    private Long quantidadePerguntas;
    private Double tempoMedioMs;

    public EstatisticaDiariaDTO(java.sql.Date data, Long quantidadePerguntas, Double tempoMedioMs) {
        this.data = data != null ? data.toLocalDate() : null;
        this.quantidadePerguntas = quantidadePerguntas;
        this.tempoMedioMs = tempoMedioMs;
    }
}
