package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_comissao_calculada_base")
public class ComissaoCalculadaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_ref", nullable = false)
    private LocalDate dateRef;

    @Column(name = "matricula", nullable = false)
    private String matricula;

    @Column(name = "cod_loja", nullable = false)
    private Integer codLoja;

    @Column(name = "cod_cargo", nullable = false)
    private Integer codCargo;

    @Column(name = "valor_base_vendas", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorBaseVendas;

    @Column(name = "percentual_aplicado", nullable = false, precision = 5, scale = 4)
    private BigDecimal percentualAplicado;

    @Column(name = "valor_comissao_gerado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorComissaoGerado;
}
