package org.trimly.backend.view.dto.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "Identificador do agendamento", example = "1") Long id,

        @Schema(
                description = "Data e hora de início do atendimento",
                example = "2027-03-15T09:00:00"
        ) LocalDateTime data,

        @Schema(description = "Duração do atendimento em minutos, herdada do serviço", example = "30") Integer duracao,

        @Schema(
                description = "Status atual; AGENDADO é o único status não terminal",
                example = "AGENDADO"
        ) AgendamentoStatus status,

        @Schema(description = "Identificador do usuário atendido", example = "2") Long usuarioId,

        @Schema(description = "Identificador do serviço agendado", example = "1") Long servicoId
) {}
