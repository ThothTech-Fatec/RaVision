package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.ComissaoCalculadaProporcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComissaoCalculadaProporcionalRepository extends JpaRepository<ComissaoCalculadaProporcional, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<ComissaoCalculadaProporcional> findByDateRef(LocalDate dateRef);
}
