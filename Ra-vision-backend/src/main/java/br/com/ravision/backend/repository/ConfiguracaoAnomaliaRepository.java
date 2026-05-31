package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.ConfiguracaoAnomalia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoAnomaliaRepository extends JpaRepository<ConfiguracaoAnomalia, Long> {

    // Retorna a configuracao ativa (assumindo que seja a mais recente ou a unica)
    Optional<ConfiguracaoAnomalia> findTopByOrderByAtualizadoEmDesc();
}
