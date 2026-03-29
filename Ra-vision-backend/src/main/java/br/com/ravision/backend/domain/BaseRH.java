package br.com.ravision.backend.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "tb_base_rh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseRH {

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

    @Column(name = "data_admiss", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "data_demiss")
    private LocalDate dataDemissao;

    @Column(name = "cod_cargo", nullable = false)
    private Integer codCargo;

    @Column(name = "descr_cargo", nullable = false)
    private String descrCargo;
}
