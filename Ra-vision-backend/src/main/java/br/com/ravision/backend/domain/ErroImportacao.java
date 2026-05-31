package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_erro_importacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErroImportacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String nomeArquivo;

    private Integer linha;

    private String coluna;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagemErro;

    @Enumerated(EnumType.STRING)
    private TipoErro tipoErro;

    @Enumerated(EnumType.STRING)
    private StatusErro status;

    private String usuarioUpload;
}
