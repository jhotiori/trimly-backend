package org.trimly.backend.model.service.disponibilidade;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;

/**
 * Validador de disponibilidades. Garante que a hora de início seja anterior à hora de fim.
 */
@Component
@RequiredArgsConstructor
public class DisponibilidadeValidator {
    /**
     * Verifica se a hora de início é anterior à hora de fim.
     *
     * @param horaInicio - hora de início da disponibilidade
     * @param horaFim - hora de fim da disponibilidade
     * @throws DisponibilidadeHorarioInvalidoException - quando a hora de início não é anterior à de fim
     */
    public void validateHorarios(LocalTime horaInicio, LocalTime horaFim) {
        if (!horaInicio.isBefore(horaFim)) {
            throw new DisponibilidadeHorarioInvalidoException(
                    "Horário de inicio deve ser menor do que o horário de fim");
        }
    }
}
