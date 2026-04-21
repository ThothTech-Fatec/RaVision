package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_comissao_calculada_final")
public class ComissaoCalculadaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_comissao_proporcional", nullable = false)
    private Long idComissaoProporcional;

    @Column(name = "date_ref", nullable = false)
    private LocalDate dateRef;

    @Column(name = "matricula", nullable = false)
    private String matricula;

    @Column(name = "valor_comissao_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComissaoFinal;

    @Column(name = "historico_regras_aplicadas", columnDefinition = "TEXT")
    private String historicoRegrasAplicadas;
}
