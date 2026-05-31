package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.MonitoramentoIA;
import br.com.ravision.backend.dto.EstatisticaDiariaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitoramentoIARepository extends JpaRepository<MonitoramentoIA, Long> {

    List<MonitoramentoIA> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime dataInicio, LocalDateTime dataFim);

    @Query("SELECT COUNT(m) FROM MonitoramentoIA m WHERE m.timestamp BETWEEN :dataInicio AND :dataFim")
    Long countByTimestampBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT AVG(m.tempoRespostaMs) FROM MonitoramentoIA m WHERE m.timestamp BETWEEN :dataInicio AND :dataFim")
    Double averageTempoRespostaBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    @Query("SELECT new br.com.ravision.backend.dto.EstatisticaDiariaDTO(" +
           "CAST(m.timestamp AS DATE), COUNT(m), AVG(m.tempoRespostaMs)) " +
           "FROM MonitoramentoIA m " +
           "WHERE m.timestamp BETWEEN :dataInicio AND :dataFim " +
           "GROUP BY CAST(m.timestamp AS DATE) " +
           "ORDER BY CAST(m.timestamp AS DATE)")
    List<EstatisticaDiariaDTO> findEstatisticasDiariasBetween(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);
}
