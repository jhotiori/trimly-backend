package org.trimly.backend.dto.agendamento;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AgendamentoCreateDTO {
    @NotNull(message = "data não pode ser nula")
    private LocalDate data;

    @NotNull(message = "horário não pode ser nulo")
    private LocalTime horario;

    @NotNull(message = "serviço não pode ser nulo")
    private Long servicoId;
}
