package org.trimly.backend.view.dto.disponibilidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;

/**
 * Dados de entrada para a criação de uma disponibilidade.
 *
 * @param diaSemana - dia da semana da janela de atendimento
 * @param horaInicio - hora de início da janela
 * @param horaFim - hora de fim da janela
 */
public record DisponibilidadeCreateDTO(
        @Schema(description = "Dia da semana em que a janela de atendimento se repete", example = "SEXTA")
        @NotNull(message = "Dia da semana não pode ser nulo")
        DiaSemana diaSemana,

        @Schema(description = "Início da janela, no formato HH:mm; deve ser anterior ao fim", example = "16:00")
        @NotNull(message = "Hora de início não pode ser nula")
        LocalTime horaInicio,

        @Schema(description = "Fim da janela, no formato HH:mm", example = "18:00")
        @NotNull(message = "Hora de fim não pode ser nula")
        LocalTime horaFim) {}
