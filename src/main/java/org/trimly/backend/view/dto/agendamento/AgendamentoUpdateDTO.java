package org.trimly.backend.view.dto.agendamento;

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
        @FutureOrPresent(message = "Horario deve estar no presente ou futuro")
        LocalDateTime data,

        AgendamentoStatus status,

        @Positive(message = "Id do Serviço deve ser positivo")
        Long servicoId) {}
