package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_comissao_calculada_proporcional")
public class ComissaoCalculadaProporcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_comissao_base", nullable = false)
    private Long idComissaoBase;

    @Column(name = "date_ref", nullable = false)
    private LocalDate dateRef;

    @Column(name = "matricula", nullable = false)
    private String matricula;

    @Column(name = "dias_do_mes", nullable = false)
    private Integer diasDoMes;

    @Column(name = "dias_trabalhados", nullable = false)
    private Integer diasTrabalhados;

    @Column(name = "motivo_proporcionalidade", nullable = false)
    private String motivoProporcionalidade;

    @Column(name = "valor_comissao_proporcional", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComissaoProporcional;
}
