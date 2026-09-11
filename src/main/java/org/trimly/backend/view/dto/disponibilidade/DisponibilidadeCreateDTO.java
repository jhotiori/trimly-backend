package org.trimly.backend.view.dto.disponibilidade;

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
        @NotNull(message = "Dia da semana não pode ser nulo")
        DiaSemana diaSemana,

        @NotNull(message = "Hora de início não pode ser nula")
        LocalTime horaInicio,

        @NotNull(message = "Hora de fim não pode ser nula") LocalTime horaFim) {}
