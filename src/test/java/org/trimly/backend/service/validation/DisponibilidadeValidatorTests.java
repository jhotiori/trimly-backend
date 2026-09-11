package org.trimly.backend.service.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeValidator;

class DisponibilidadeValidatorTests {
    private final DisponibilidadeValidator validator = new DisponibilidadeValidator();

    /**
     * Verifica que uma hora de início anterior à hora de fim não lança exceção.
     */
    @Test
    void disponibilidade_TestValidateHorariosComOrdemValidaNaoLancaExcecao() {
        assertThatCode(() -> validator.validateHorarios(LocalTime.of(9, 0), LocalTime.of(18, 0)))
                .doesNotThrowAnyException();
    }

    /**
     * Verifica que uma hora de início igual à hora de fim lança {@link DisponibilidadeHorarioInvalidoException}.
     */
    @Test
    void disponibilidade_TestValidateHorariosComInicioIgualAoFimLancaExcecao() {
        LocalTime horario = LocalTime.of(10, 0);

        assertThatThrownBy(() -> validator.validateHorarios(horario, horario))
                .isInstanceOf(DisponibilidadeHorarioInvalidoException.class);
    }

    /**
     * Verifica que uma hora de início posterior à hora de fim lança {@link DisponibilidadeHorarioInvalidoException}.
     */
    @Test
    void disponibilidade_TestValidateHorariosComInicioAposFimLancaExcecao() {
        assertThatThrownBy(() -> validator.validateHorarios(LocalTime.of(18, 0), LocalTime.of(9, 0)))
                .isInstanceOf(DisponibilidadeHorarioInvalidoException.class);
    }
}
