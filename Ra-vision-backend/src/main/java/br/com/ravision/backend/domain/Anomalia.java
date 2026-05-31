package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_anomalia_comissao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomalia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_ref", nullable = false)
    private LocalDate dateRef;

    @Column(name = "matricula", nullable = false)
    private String matricula;

    @Column(name = "cod_cargo", nullable = false)
    private Integer codCargo;

    @Column(name = "valor_comissao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComissao;

    @Column(name = "limite_esperado", precision = 15, scale = 2)
    private BigDecimal limiteEsperado;

    @Column(name = "valor_medio", precision = 15, scale = 2)
    private BigDecimal valorMedio; // Usado para Z-score ou estatisticas simples

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_anomalia", nullable = false)
    private TipoAnomalia tipoAnomalia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_analise", nullable = false)
    private StatusAnaliseAnomalia statusAnalise;

    @Column(name = "justificativa", columnDefinition = "TEXT")
    private String justificativa;

    @PrePersist
    public void prePersist() {
        if (this.statusAnalise == null) {
            this.statusAnalise = StatusAnaliseAnomalia.PENDENTE;
        }
    }
}
