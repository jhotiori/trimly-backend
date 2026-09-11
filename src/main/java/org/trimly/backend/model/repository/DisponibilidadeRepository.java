package org.trimly.backend.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;

/**
 * Repositório de disponibilidades, com busca das janelas de atendimento por dia da semana.
 */
public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeEntity, Long> {
    /**
     * Retorna as disponibilidades cadastradas para o dia da semana informado.
     *
     * @param diaSemana - dia da semana usado no filtro
     * @return List - disponibilidades desse dia, vazia quando não houver nenhuma
     */
    List<DisponibilidadeEntity> findByDiaSemana(DiaSemana diaSemana);
}
