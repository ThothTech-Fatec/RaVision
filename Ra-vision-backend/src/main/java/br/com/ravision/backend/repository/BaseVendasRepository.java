package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.BaseVendas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BaseVendasRepository extends JpaRepository<BaseVendas, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<BaseVendas> findByDateRef(LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(v.vlrVenda) FROM BaseVendas v WHERE v.dateRef = :dateRef")
    java.math.BigDecimal sumarizarFaturamentoGeral(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.DashboardAgregacaoDTO(v.descrMarca, SUM(v.vlrVenda)) " +
           "FROM BaseVendas v WHERE v.dateRef = :dateRef GROUP BY v.descrMarca ORDER BY SUM(v.vlrVenda) DESC")
    List<br.com.ravision.backend.dto.DashboardAgregacaoDTO> sumarizarFaturamentoPorMarca(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.HistoricoEvolucaoDTO(v.dateRef, SUM(v.vlrVenda), CAST(0 AS big_decimal)) " +
           "FROM BaseVendas v WHERE v.dateRef BETWEEN :startDate AND :endDate GROUP BY v.dateRef ORDER BY v.dateRef ASC")
    List<br.com.ravision.backend.dto.HistoricoEvolucaoDTO> historicoFaturamento(@org.springframework.data.repository.query.Param("startDate") LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);
}
