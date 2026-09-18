package org.trimly.backend.view.dto.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;

/**
 * Campos atualizáveis de um agendamento. Todos são opcionais (semântica de PATCH); uma requisição
 * com todos nulos é um no-op.
 *
 * @param data - nova data e hora de início
 * @param status - novo status
 * @param servicoId - identificador do novo serviço
 */
public record AgendamentoUpdateDTO(
        @Schema(
                description = "Opcional. Nova data e hora de início, no formato yyyy-MM-ddTHH:mm:ss",
                example = "2027-03-16T14:00:00"
        ) @FutureOrPresent(message = "Horario deve estar no presente ou futuro") LocalDateTime data,

        @Schema(
                description = """
                        Opcional. Novo status; de AGENDADO pode ir para CANCELADO, CONCLUIDO ou AUSENTE""",
                example = "CANCELADO"
        ) AgendamentoStatus status,

        @Schema(description = "Opcional. Identificador do novo serviço", example = "2") @Positive(
                message = "Id do Serviço deve ser positivo"
        ) Long servicoId
) {}
