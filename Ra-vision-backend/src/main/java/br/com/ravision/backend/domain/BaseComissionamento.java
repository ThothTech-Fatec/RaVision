package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_base_comissionamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseComissionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_marca", nullable = false)
    private Integer codMarca;

    @Column(name = "descr_marca", nullable = false)
    private String descrMarca;

    @Column(name = "cod_cargo", nullable = false)
    private Integer codCargo;

    @Column(name = "descri_cargo", nullable = false)
    private String descriCargo;

    @Column(name = "pct_comiss", nullable = false)
    private BigDecimal percentualComissao;
}
