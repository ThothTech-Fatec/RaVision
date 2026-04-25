package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.RegraNegocioDinamica;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ravision.backend.domain.StatusAprovacao;
import java.util.List;

public interface RegraNegocioDinamicaRepository extends JpaRepository<RegraNegocioDinamica, Long> {
    List<RegraNegocioDinamica> findByMesCompetenciaAndStatusAprovacao(String mesCompetencia, StatusAprovacao statusAprovacao);
}
