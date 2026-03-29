package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_base_vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseVendas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_ref", nullable = false)
    private LocalDate dateRef;

    @Column(name = "cod_marca", nullable = false)
    private Integer codMarca;

    @Column(name = "descr_marca", nullable = false)
    private String descrMarca;

    @Column(name = "cod_loja", nullable = false)
    private Integer codLoja;

    @Column(name = "descr_loja", nullable = false)
    private String descrLoja;

    @Column(name = "matricula", nullable = false)
    private String matricula;

    @Column(name = "vlr_venda", nullable = false)
    private BigDecimal vlrVenda;
}
