package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.ErroImportacao;
import br.com.ravision.backend.domain.StatusErro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ErroImportacaoRepository extends JpaRepository<ErroImportacao, Long> {

    @Query("SELECT e FROM ErroImportacao e WHERE " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(CAST(:nomeArquivo AS string) IS NULL OR LOWER(e.nomeArquivo) LIKE LOWER(CONCAT('%', CAST(:nomeArquivo AS string), '%'))) AND " +
           "(CAST(:dataInicio AS java.time.LocalDateTime) IS NULL OR e.timestamp >= :dataInicio) AND " +
           "(CAST(:dataFim AS java.time.LocalDateTime) IS NULL OR e.timestamp <= :dataFim) " +
           "ORDER BY e.timestamp DESC")
    Page<ErroImportacao> findByFiltros(
            @Param("status") StatusErro status,
            @Param("nomeArquivo") String nomeArquivo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    long countByStatus(StatusErro status);
}
