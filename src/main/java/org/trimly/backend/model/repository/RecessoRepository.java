package org.trimly.backend.model.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.model.entity.recesso.RecessoEntity;

/**
 * Repositório de recessos.
 */
public interface RecessoRepository extends JpaRepository<RecessoEntity, Long> {
    /**
     * Retorna os recessos que se sobrepõem ao período informado, com as duas pontas inclusivas.
     *
     * @param dataFim - último dia do período consultado
     * @param dataInicio - primeiro dia do período consultado
     * @return List - recessos que tocam o período, vazia quando não houver nenhum
     */
    List<RecessoEntity> findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(
            LocalDate dataFim,
            LocalDate dataInicio
    );
}
