package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.IntercorrenciaRH;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntercorrenciaRHRepository extends JpaRepository<IntercorrenciaRH, Long> {
    List<IntercorrenciaRH> findByMatriculaAndTipo(String matricula, String tipo);
}
