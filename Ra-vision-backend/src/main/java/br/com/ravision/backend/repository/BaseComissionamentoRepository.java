package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.BaseComissionamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseComissionamentoRepository extends JpaRepository<BaseComissionamento, Long> {
    Optional<BaseComissionamento> findByCodMarcaAndCodCargo(Integer codMarca, Integer codCargo);
}
