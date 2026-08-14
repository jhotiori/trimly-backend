package org.trimly.backend.dto.agendamento;

import org.trimly.backend.entity.enums.StatusAgendamento;
import java.time.LocalDateTime;

public record AgendamentoResponseDTO(
    Long id,
    LocalDateTime horario,
    Integer duracao,
    StatusAgendamento status,
    Long usuarioId,
    Long servicoId
) {}