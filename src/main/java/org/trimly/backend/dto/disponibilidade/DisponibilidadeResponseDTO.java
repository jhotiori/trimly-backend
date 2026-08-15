package org.trimly.backend.dto.disponibilidade;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DisponibilidadeResponseDTO(
    Long id,
    DayOfWeek diaSemana,
    LocalTime horaInicio,
    LocalTime horaFim
) {}