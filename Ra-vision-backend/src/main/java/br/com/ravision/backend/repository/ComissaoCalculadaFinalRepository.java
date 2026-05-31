package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.ComissaoCalculadaFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComissaoCalculadaFinalRepository extends JpaRepository<ComissaoCalculadaFinal, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<ComissaoCalculadaFinal> findByDateRef(LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.RelatorioFolhaDTO(" +
           "f.matricula, rh.descrLoja, p.diasTrabalhados, p.diasDoMes, p.motivoProporcionalidade, f.valorComissaoFinal) " +
           "FROM ComissaoCalculadaFinal f " +
           "JOIN ComissaoCalculadaProporcional p ON f.idComissaoProporcional = p.id " +
           "LEFT JOIN BaseRH rh ON rh.matricula = f.matricula AND rh.dateRef = f.dateRef " +
           "WHERE f.dateRef = :dateRef")
    List<br.com.ravision.backend.dto.RelatorioFolhaDTO> findRelatorioByDateRef(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.DashboardAgregacaoDTO(b.descrLoja, SUM(c.valorComissaoFinal)) " +
           "FROM ComissaoCalculadaFinal c " +
           "JOIN BaseRH b ON c.matricula = b.matricula AND c.dateRef = b.dateRef " +
           "WHERE c.dateRef = :dateRef " +
           "GROUP BY b.descrLoja " +
           "ORDER BY SUM(c.valorComissaoFinal) DESC")
    List<br.com.ravision.backend.dto.DashboardAgregacaoDTO> sumarizarComissoesPorLoja(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.DashboardAgregacaoDTO(b.descrMarca, SUM(c.valorComissaoFinal)) " +
           "FROM ComissaoCalculadaFinal c " +
           "JOIN BaseRH b ON c.matricula = b.matricula AND c.dateRef = b.dateRef " +
           "WHERE c.dateRef = :dateRef " +
           "GROUP BY b.descrMarca " +
           "ORDER BY SUM(c.valorComissaoFinal) DESC")
    List<br.com.ravision.backend.dto.DashboardAgregacaoDTO> sumarizarComissoesPorMarca(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(c.valorComissaoFinal) " +
           "FROM ComissaoCalculadaFinal c " +
           "WHERE c.dateRef = :dateRef")
    java.math.BigDecimal sumarizarComissaoGeral(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.RankingVendedorDTO(c.matricula, c.matricula, b.descrLoja, CAST(0 AS big_decimal), c.valorComissaoFinal) " +
           "FROM ComissaoCalculadaFinal c " +
           "JOIN BaseRH b ON c.matricula = b.matricula AND c.dateRef = b.dateRef " +
           "WHERE c.dateRef = :dateRef " +
           "ORDER BY c.valorComissaoFinal DESC")
    List<br.com.ravision.backend.dto.RankingVendedorDTO> rankingVendedores(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.HistoricoEvolucaoDTO(c.dateRef, CAST(0 AS big_decimal), SUM(c.valorComissaoFinal)) " +
           "FROM ComissaoCalculadaFinal c " +
           "WHERE c.dateRef BETWEEN :startDate AND :endDate " +
           "GROUP BY c.dateRef " +
           "ORDER BY c.dateRef ASC")
    List<br.com.ravision.backend.dto.HistoricoEvolucaoDTO> historicoComissoes(@org.springframework.data.repository.query.Param("startDate") LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);
}
