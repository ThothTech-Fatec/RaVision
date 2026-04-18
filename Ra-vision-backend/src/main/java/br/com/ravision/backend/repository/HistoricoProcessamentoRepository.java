package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.HistoricoProcessamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoProcessamentoRepository extends JpaRepository<HistoricoProcessamento, Long> {
    List<HistoricoProcessamento> findAllByOrderByDataHoraDesc();
}
