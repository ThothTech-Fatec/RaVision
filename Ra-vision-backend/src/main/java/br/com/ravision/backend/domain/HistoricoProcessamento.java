package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_processamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "acao_realizada", nullable = false)
    private String acaoRealizada;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "detalhes_acao", columnDefinition = "TEXT")
    private String detalhesAcao;
}
