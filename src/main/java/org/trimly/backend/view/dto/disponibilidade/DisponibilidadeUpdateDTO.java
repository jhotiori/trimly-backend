package org.trimly.backend.view.dto.disponibilidade;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;

/**
 * Campos atualizáveis de uma disponibilidade. Todos são opcionais (semântica de PATCH); uma
 * requisição com todos nulos é um no-op.
 *
 * @param diaSemana - novo dia da semana
 * @param horaInicio - nova hora de início
 * @param horaFim - nova hora de fim
 */
public record DisponibilidadeUpdateDTO(
        @Schema(description = "Opcional. Novo dia da semana da janela", example = "SEXTA") DiaSemana diaSemana,

        @Schema(
                description = "Opcional. Nova hora de início, no formato HH:mm",
                example = "12:00"
        ) LocalTime horaInicio,

        @Schema(description = "Opcional. Nova hora de fim, no formato HH:mm", example = "16:00") LocalTime horaFim
) {}
