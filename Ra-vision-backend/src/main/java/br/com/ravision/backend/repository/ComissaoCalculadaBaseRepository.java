package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.ComissaoCalculadaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComissaoCalculadaBaseRepository extends JpaRepository<ComissaoCalculadaBase, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<ComissaoCalculadaBase> findByDateRef(LocalDate dateRef);
}
