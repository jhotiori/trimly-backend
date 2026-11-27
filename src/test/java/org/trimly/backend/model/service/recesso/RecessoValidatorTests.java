package org.trimly.backend.model.service.recesso;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.recesso.RecessoConflitoException;
import org.trimly.backend.model.exception.recesso.RecessoNoPassadoException;
import org.trimly.backend.model.exception.recesso.RecessoPeriodoInvalidoException;
import org.trimly.backend.model.repository.RecessoRepository;

/**
 * Testes para {@link RecessoValidator}.
 */
@ExtendWith(MockitoExtension.class)
class RecessoValidatorTests {
    /**
     * Primeiro dia do período usado nos cenários.
     */
    private static final LocalDate INICIO = LocalDate.of(2030, 12, 1);

    /**
     * Último dia do período usado nos cenários.
     */
    private static final LocalDate FIM = LocalDate.of(2030, 12, 17);

    /**
     * Repositório de recessos simulado.
     * @see {@link RecessoRepository}
     */
    @Mock
    private RecessoRepository repository;

    /**
     * Validador sob teste, com os mocks injetados.
     * @see {@link RecessoValidator}
     */
    @InjectMocks
    private RecessoValidator validator;

    @Test
    @DisplayName("Aceita hoje e datas futuras")
    void testeDadoHojeOuFuturoQuandoValidarNoPassadoEntaoNaoLanca() {
        assertAll(
                () -> assertDoesNotThrow(() -> validator.validateNoPassado(LocalDate.now())),
                () -> assertDoesNotThrow(() -> validator.validateNoPassado(LocalDate.now().plusDays(1)))
        );
    }

    @Test
    @DisplayName("Rejeita data de ontem")
    void testeDadoOntemQuandoValidarNoPassadoEntaoLancaNoPassado() {
        LocalDate ontem = LocalDate.now().minusDays(1);

        assertThrows(RecessoNoPassadoException.class, () -> validator.validateNoPassado(ontem));
    }

    @Test
    @DisplayName("Aceita fim igual ou posterior ao início, inclusive intervalo acima de 14 dias")
    void testeDadoFimNaoAnteriorAoInicioQuandoValidarPeriodoEntaoNaoLanca() {
        assertAll(
                () -> assertDoesNotThrow(() -> validator.validatePeriodo(INICIO, INICIO)),
                () -> assertDoesNotThrow(() -> validator.validatePeriodo(INICIO, FIM)),
                () -> assertDoesNotThrow(() -> validator.validatePeriodo(INICIO, INICIO.plusDays(60)))
        );
    }

    @Test
    @DisplayName("Rejeita fim anterior ao início")
    void testeDadoFimAntesDoInicioQuandoValidarPeriodoEntaoLancaPeriodoInvalido() {
        LocalDate diaAntes = INICIO.minusDays(1);

        assertThrows(RecessoPeriodoInvalidoException.class, () -> validator.validatePeriodo(INICIO, diaAntes));
    }

    @Test
    @DisplayName("Aceita período quando não há recesso sobreposto")
    void testeDadoSemSobreposicaoQuandoValidarConflitoEntaoNaoLanca() {
        when(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM, INICIO)).thenReturn(List.of());

        assertDoesNotThrow(() -> validator.validateConflito(null, INICIO, FIM));
    }

    @Test
    @DisplayName("Rejeita período que se sobrepõe a outro recesso")
    void testeDadoSobreposicaoQuandoValidarConflitoEntaoLancaConflito() {
        when(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM, INICIO))
                .thenReturn(List.of(recesso(10L)));

        assertThrows(RecessoConflitoException.class, () -> validator.validateConflito(null, INICIO, FIM));
    }

    @Test
    @DisplayName("Ignora o próprio recesso ao atualizar")
    void testeDadoMesmoIdQuandoValidarConflitoEntaoIgnoraOProprioRecesso() {
        when(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM, INICIO))
                .thenReturn(List.of(recesso(10L)));

        assertDoesNotThrow(() -> validator.validateConflito(10L, INICIO, FIM));
    }

    @Test
    @DisplayName("Rejeita ao atualizar quando outro recesso se sobrepõe")
    void testeDadoOutroIdQuandoValidarConflitoEntaoLancaConflito() {
        when(repository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(FIM, INICIO))
                .thenReturn(List.of(recesso(10L)));

        assertThrows(RecessoConflitoException.class, () -> validator.validateConflito(11L, INICIO, FIM));
    }

    private static RecessoEntity recesso(Long id) {
        return RecessoEntity.builder().id(id).descricao("Férias").dataInicio(INICIO).dataFim(FIM).build();
    }
}
