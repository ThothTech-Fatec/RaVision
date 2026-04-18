package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_regra_negocio_dinamica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegraNegocioDinamica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao_regra", nullable = false)
    private String descricaoRegra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_regra", nullable = false)
    private TipoRegra tipoRegra;

    @Column(name = "mes_competencia", nullable = false)
    private String mesCompetencia;

    @Column(name = "condicoes_aplicacao", columnDefinition = "TEXT")
    private String condicoesAplicacao;

    @Column(name = "valor_modificador", nullable = false, precision = 19, scale = 2)
    private BigDecimal valorModificador;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false)
    private StatusAprovacao statusAprovacao;

    @Column(name = "criado_por", nullable = false)
    private String criadoPor;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;
    
    @PrePersist
    public void prePersist() {
        if (this.dataCriacao == null) {
            this.dataCriacao = LocalDateTime.now();
        }
        if (this.statusAprovacao == null) {
            this.statusAprovacao = StatusAprovacao.PENDENTE;
        }
    }
}
