package org.trimly.backend.view.dto.agendamento;

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
        @NotNull(message = "Data não pode ser nula") @FutureOrPresent(message = "Data deve estar no presente ou futuro")
        LocalDate data,

        @NotNull(message = "Horário não pode ser nulo") LocalTime horario,

        @NotNull(message = "Usuário não pode ser nulo") @Positive(message = "Id de Usuário deve ser positivo")
        Long usuarioId,

        @NotNull(message = "Serviço não pode ser nulo") @Positive(message = "Id de Serviço deve ser positivo")
        Long servicoId) {}
