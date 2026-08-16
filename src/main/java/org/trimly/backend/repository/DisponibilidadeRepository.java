package org.trimly.backend.repository;

import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.entity.DisponibilidadeEntity;

public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeEntity, Long> {
    List<DisponibilidadeEntity> findByDiaSemana(DayOfWeek diaSemana);
}
