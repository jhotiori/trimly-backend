package org.trimly.backend.view.dto.disponibilidade;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;

/**
 * Representação de resposta de uma disponibilidade.
 *
 * @param id - identificador da disponibilidade
 * @param diaSemana - dia da semana da janela de atendimento
 * @param horaInicio - hora de início da janela
 * @param horaFim - hora de fim da janela
 */
public record DisponibilidadeResponseDTO(
        @Schema(description = "Identificador da disponibilidade", example = "1")
        Long id,

        @Schema(description = "Dia da semana em que a janela de atendimento se repete", example = "SEGUNDA")
        DiaSemana diaSemana,

        @Schema(description = "Início da janela", example = "07:00:00")
        LocalTime horaInicio,

        @Schema(description = "Fim da janela", example = "12:00:00")
        LocalTime horaFim) {}
