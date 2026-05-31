package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_monitoramento_ia", indexes = {
    @Index(name = "idx_monitoramento_data", columnList = "timestamp"),
    @Index(name = "idx_monitoramento_usuario", columnList = "usuarioLogado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoramentoIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String perguntaUsuario;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String respostaIA;
    
    @Column(nullable = false)
    private Long tempoRespostaMs;
    
    private String ferramentaUtilizada;
    
    @Column(nullable = false)
    private Boolean sucessoFerramenta;
    
    @Column(nullable = false)
    private String usuarioLogado;
}
