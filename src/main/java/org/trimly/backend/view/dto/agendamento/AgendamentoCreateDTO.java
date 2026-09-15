package org.trimly.backend.view.dto.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados de entrada para a criação de um agendamento.
 *
 * @param data - dia do agendamento
 * @param horario - hora de início do agendamento
 * @param usuarioId - identificador do usuário dono do agendamento
 * @param servicoId - identificador do serviço agendado
 */
public record AgendamentoCreateDTO(
        @Schema(description = "Dia do atendimento, no formato yyyy-MM-dd", example = "2027-03-15")
        @NotNull(message = "Data não pode ser nula")
        @FutureOrPresent(message = "Data deve estar no presente ou futuro")
        LocalDate data,

        @Schema(
                description = "Hora de início, no formato HH:mm; o fim é calculado pela duração do serviço",
                example = "09:00")
        @NotNull(message = "Horário não pode ser nulo")
        LocalTime horario,

        @Schema(description = "Identificador do usuário atendido", example = "2")
        @NotNull(message = "Usuário não pode ser nulo")
        @Positive(message = "Id de Usuário deve ser positivo")
        Long usuarioId,

        @Schema(description = "Identificador do serviço a ser realizado", example = "1")
        @NotNull(message = "Serviço não pode ser nulo")
        @Positive(message = "Id de Serviço deve ser positivo")
        Long servicoId) {}
