package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.BaseRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BaseRHRepository extends JpaRepository<BaseRH, Long> {
    List<BaseRH> findByDateRef(LocalDate dateRef);
    void deleteByDateRef(LocalDate dateRef);
}
