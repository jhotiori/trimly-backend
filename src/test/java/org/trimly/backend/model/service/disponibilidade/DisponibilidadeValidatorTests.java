package org.trimly.backend.model.service.disponibilidade;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;
import org.trimly.backend.model.repository.DisponibilidadeRepository;

/**
 * Testes para {@link DisponibilidadeValidator}.
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadeValidatorTests {
    /**
     * Repositório de disponibilidades simulado.
     * @see {@link DisponibilidadeRepository}
     */
    @Mock
    private DisponibilidadeRepository repository;

    /**
     * Validador sob teste, com os mocks injetados.
     * @see {@link DisponibilidadeValidator}
     */
    @InjectMocks
    private DisponibilidadeValidator validator;

    @Test
    @DisplayName("Aceita início anterior ao fim")
    void testeDadoInicioAntesDoFimQuandoValidarHorariosEntaoNaoLanca() {
        assertDoesNotThrow(() -> validator.validateHorarios(LocalTime.of(8, 0), LocalTime.of(12, 0)));
    }

    @Test
    @DisplayName("Rejeita início igual ou posterior ao fim")
    void testeDadoInicioNaoAnteriorAoFimQuandoValidarHorariosEntaoLancaHorarioInvalido() {
        LocalTime meioDia = LocalTime.of(12, 0);
        LocalTime umaDaTarde = LocalTime.of(13, 0);

        assertAll(
                () -> assertThrows(
                        DisponibilidadeHorarioInvalidoException.class,
                        () -> validator.validateHorarios(meioDia, meioDia)
                ),
                () -> assertThrows(
                        DisponibilidadeHorarioInvalidoException.class,
                        () -> validator.validateHorarios(umaDaTarde, meioDia)
                )
        );
    }

    @Test
    @DisplayName("Aceita janela quando o dia não tem outras")
    void testeDadoDiaSemOutrasJanelasQuandoValidarConflitoEntaoNaoLanca() {
        when(repository.findByDiaSemana(DiaSemana.SEXTA)).thenReturn(List.of());

        assertDoesNotThrow(
                () -> validator.validateConflitoDeHorario(null, DiaSemana.SEXTA, LocalTime.of(8, 0), LocalTime.of(12, 0))
        );
    }

    @Test
    @DisplayName("Rejeita janela que sobrepõe outra do mesmo dia")
    void testeDadoSobreposicaoQuandoValidarConflitoEntaoLancaConflito() {
        when(repository.findByDiaSemana(DiaSemana.SEXTA)).thenReturn(List.of(janela(10L, 9, 12)));

        LocalTime inicio = LocalTime.of(11, 0);
        LocalTime fim = LocalTime.of(14, 0);

        assertThrows(
                DisponibilidadeConflitoException.class,
                () -> validator.validateConflitoDeHorario(null, DiaSemana.SEXTA, inicio, fim)
        );
    }

    @Test
    @DisplayName("Aceita janelas que apenas encostam")
    void testeDadoJanelasAdjacentesQuandoValidarConflitoEntaoNaoLanca() {
        when(repository.findByDiaSemana(DiaSemana.SEXTA)).thenReturn(List.of(janela(10L, 9, 12)));

        assertAll(
                () -> assertDoesNotThrow(
                        () -> validator
                                .validateConflitoDeHorario(null, DiaSemana.SEXTA, LocalTime.of(12, 0), LocalTime.of(14, 0))
                ),
                () -> assertDoesNotThrow(
                        () -> validator
                                .validateConflitoDeHorario(null, DiaSemana.SEXTA, LocalTime.of(7, 0), LocalTime.of(9, 0))
                )
        );
    }

    @Test
    @DisplayName("Ignora a própria janela ao atualizar")
    void testeDadoMesmoIdQuandoValidarConflitoEntaoIgnoraAPropriaJanela() {
        when(repository.findByDiaSemana(DiaSemana.SEXTA)).thenReturn(List.of(janela(10L, 9, 12)));

        assertDoesNotThrow(
                () -> validator.validateConflitoDeHorario(10L, DiaSemana.SEXTA, LocalTime.of(9, 0), LocalTime.of(13, 0))
        );
    }

    private static DisponibilidadeEntity janela(Long id, int horaInicio, int horaFim) {
        return DisponibilidadeEntity.builder()
                .id(id)
                .diaSemana(DiaSemana.SEXTA)
                .horaInicio(LocalTime.of(horaInicio, 0))
                .horaFim(LocalTime.of(horaFim, 0))
                .build();
    }
}
