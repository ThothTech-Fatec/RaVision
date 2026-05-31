package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_configuracao_anomalia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoAnomalia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "algoritmo", nullable = false)
    private TipoAlgoritmo algoritmo;

    @Column(name = "limite_multiplicador", nullable = false)
    private Double limiteMultiplicador;

    @Column(name = "agrupar_por_cargo", nullable = false)
    private Boolean agruparPorCargo;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
