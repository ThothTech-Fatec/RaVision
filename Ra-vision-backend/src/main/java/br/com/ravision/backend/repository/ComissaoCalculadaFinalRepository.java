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
}
