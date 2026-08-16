package org.trimly.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trimly.backend.entity.AgendamentoEntity;
import org.trimly.backend.entity.UsuarioEntity;
import org.trimly.backend.entity.enums.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {
    List<AgendamentoEntity> findByUsuario(UsuarioEntity usuario);

    List<AgendamentoEntity> findByHorarioBetween(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    @Query("""
        SELECT a
        FROM AgendamentoEntity a
        WHERE a.horario < :fim
          AND a.status = :status
    """)
    List<AgendamentoEntity> findAgendamentosAtivosAntesDe(
            @Param("fim") LocalDateTime fim,
            @Param("status") StatusAgendamento status
    );
}
