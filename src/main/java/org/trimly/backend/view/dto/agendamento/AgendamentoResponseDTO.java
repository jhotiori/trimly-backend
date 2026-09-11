package org.trimly.backend.view.dto.agendamento;

import java.time.LocalDateTime;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;

/**
 * Representação de resposta de um agendamento.
 *
 * @param id - identificador do agendamento
 * @param data - data e hora de início
 * @param duracao - duração em minutos
 * @param status - status atual do agendamento
 * @param usuarioId - identificador do usuário dono do agendamento
 * @param servicoId - identificador do serviço agendado
 */
public record AgendamentoResponseDTO(
        Long id, LocalDateTime data, Integer duracao, AgendamentoStatus status, Long usuarioId, Long servicoId) {}
