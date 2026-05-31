package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.IntercorrenciaRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercorrenciaRHRepository extends JpaRepository<IntercorrenciaRH, Long> {
    List<IntercorrenciaRH> findByMatriculaAndTipo(String matricula, String tipo);

    @org.springframework.data.jpa.repository.Query("SELECT new br.com.ravision.backend.dto.DashboardAgregacaoDTO(i.tipo, CAST(COUNT(i.id) AS big_decimal)) " +
           "FROM IntercorrenciaRH i " +
           "WHERE i.dataInicio <= :endDate AND i.dataFim >= :startDate " +
           "GROUP BY i.tipo")
    List<br.com.ravision.backend.dto.DashboardAgregacaoDTO> sumarizarProporcionalidade(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
