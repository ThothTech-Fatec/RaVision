package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.Anomalia;
import br.com.ravision.backend.domain.StatusAnaliseAnomalia;
import br.com.ravision.backend.domain.TipoAnomalia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AnomaliaRepository extends JpaRepository<Anomalia, Long> {

    @Modifying
    @Query("DELETE FROM Anomalia a WHERE a.dateRef = :dateRef AND a.statusAnalise = 'PENDENTE'")
    void deletePendenteByDateRef(@Param("dateRef") LocalDate dateRef);

    @Query("SELECT a FROM Anomalia a WHERE " +
           "(cast(:dateRef as date) IS NULL OR a.dateRef = :dateRef) AND " +
           "(:matricula IS NULL OR a.matricula = :matricula) AND " +
           "(cast(:tipoAnomalia as string) IS NULL OR a.tipoAnomalia = :tipoAnomalia) AND " +
           "(cast(:statusAnalise as string) IS NULL OR a.statusAnalise = :statusAnalise)")
    Page<Anomalia> findWithFilters(
            @Param("dateRef") LocalDate dateRef,
            @Param("matricula") String matricula,
            @Param("tipoAnomalia") TipoAnomalia tipoAnomalia,
            @Param("statusAnalise") StatusAnaliseAnomalia statusAnalise,
            Pageable pageable);
}
