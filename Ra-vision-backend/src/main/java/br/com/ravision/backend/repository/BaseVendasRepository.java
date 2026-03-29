package br.com.ravision.backend.repository;

import br.com.ravision.backend.domain.BaseVendas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BaseVendasRepository extends JpaRepository<BaseVendas, Long> {
    void deleteByDateRef(LocalDate dateRef);
    List<BaseVendas> findByDateRef(LocalDate dateRef);
}
