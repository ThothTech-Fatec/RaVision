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
}
