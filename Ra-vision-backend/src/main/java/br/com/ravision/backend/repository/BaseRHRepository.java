package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.BaseRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BaseRHRepository extends JpaRepository<BaseRH, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<BaseRH> findByDateRef(LocalDate dateRef);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT r.dateRef FROM BaseRH r ORDER BY r.dateRef DESC")
    List<LocalDate> findDistinctDateRefs();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r.id) FROM BaseRH r WHERE r.dateRef = :dateRef AND (r.descrLoja IS NULL OR r.descrLoja = '')")
    Long contarAnomaliasSemLoja(@org.springframework.data.repository.query.Param("dateRef") LocalDate dateRef);
}
