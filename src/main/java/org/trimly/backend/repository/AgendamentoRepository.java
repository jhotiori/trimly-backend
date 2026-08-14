package org.trimly.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.entity.AgendamentoEntity;
import org.trimly.backend.entity.UsuarioEntity;
import java.time.LocalDateTime;

public interface AgendamentoRepository extends JpaRepository<AgendamentoEntity, Long> {
    List<AgendamentoEntity> findByUsuario(UsuarioEntity usuario);

    List<AgendamentoEntity> findByHorario(LocalDateTime horario);

    List<AgendamentoEntity> findByHorarioBetween(LocalDateTime inicio, LocalDateTime fim);
}
