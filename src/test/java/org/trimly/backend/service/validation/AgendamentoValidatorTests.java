package org.trimly.backend.service.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.AgendamentoEntity;
import org.trimly.backend.model.entity.DisponibilidadeEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.entity.enums.DiaSemana;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoForaDoHorarioException;
import org.trimly.backend.model.exception.agendamento.AgendamentoSemDisponibilidadeException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.service.agendamento.AgendamentoValidator;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;

@ExtendWith(MockitoExtension.class)
class AgendamentoValidatorTests {
    @Mock
    private AgendamentoRepository repository;

    @Mock
    private DisponibilidadeService disponibilidadeService;

    @InjectMocks
    private AgendamentoValidator validator;

    /**
     * Verifica que um agendamento que não está em {@code AGENDADO} lança {@link AgendamentoStatusException} ao
     * tentar ser alterado.
     */
    @Test
    void agendamento_TestValidateStatusUpdateComOrigemDiferenteDeAgendadoLancaExcecao() {
        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setStatus(AgendamentoStatus.CANCELADO);

        assertThatThrownBy(() -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.CONCLUIDO))
                .isInstanceOf(AgendamentoStatusException.class);
    }

    /**
     * Verifica que solicitar o mesmo status já atual do agendamento lança {@link AgendamentoStatusException}.
     */
    @Test
    void agendamento_TestValidateStatusUpdateComNovoStatusIgualAoAtualLancaExcecao() {
        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setStatus(AgendamentoStatus.AGENDADO);

        assertThatThrownBy(() -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.AGENDADO))
                .isInstanceOf(AgendamentoStatusException.class);
    }

    /**
     * Verifica que alterar um agendamento em {@code AGENDADO} para um status diferente não lança exceção.
     */
    @Test
    void agendamento_TestValidateStatusUpdateComOrigemAgendadoENovoDiferenteNaoLancaExcecao() {
        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setStatus(AgendamentoStatus.AGENDADO);

        assertThatCode(() -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.CANCELADO))
                .doesNotThrowAnyException();
    }

    /**
     * Verifica que um agendamento cujo início e fim caem no mesmo dia não lança exceção.
     */
    @Test
    void agendamento_TestValidateHorarioFuturoNoMesmoDiaNaoLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);

        assertThatCode(() -> validator.validateHorarioFuturo(inicio, fim)).doesNotThrowAnyException();
    }

    /**
     * Verifica que um agendamento que ultrapassa a virada do dia lança {@link AgendamentoForaDoHorarioException}.
     */
    @Test
    void agendamento_TestValidateHorarioFuturoUltrapassandoMeiaNoiteLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 23, 30);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 11, 0, 30);

        assertThatThrownBy(() -> validator.validateHorarioFuturo(inicio, fim))
                .isInstanceOf(AgendamentoForaDoHorarioException.class);
    }

    /**
     * Verifica que um agendamento que cabe inteiramente em uma disponibilidade do dia não lança exceção.
     */
    @Test
    void agendamento_TestValidateDisponibilidadeDentroDeUmaJanelaNaoLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);
        DisponibilidadeEntity disponibilidade = new DisponibilidadeEntity();
        disponibilidade.setDiaSemana(DiaSemana.QUINTA);
        disponibilidade.setHoraInicio(LocalTime.of(9, 0));
        disponibilidade.setHoraFim(LocalTime.of(18, 0));

        when(disponibilidadeService.findByDiaSemana(any())).thenReturn(List.of(disponibilidade));

        assertThatCode(() -> validator.validateDisponibilidade(inicio, fim)).doesNotThrowAnyException();
    }

    /**
     * Verifica que a ausência de qualquer disponibilidade no dia do agendamento lança
     * {@link AgendamentoSemDisponibilidadeException}.
     */
    @Test
    void agendamento_TestValidateDisponibilidadeSemDisponibilidadeNoDiaLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);

        when(disponibilidadeService.findByDiaSemana(any())).thenReturn(List.of());

        assertThatThrownBy(() -> validator.validateDisponibilidade(inicio, fim))
                .isInstanceOf(AgendamentoSemDisponibilidadeException.class);
    }

    /**
     * Verifica que um agendamento fora do horário de todas as disponibilidades do dia lança
     * {@link AgendamentoSemDisponibilidadeException}.
     */
    @Test
    void agendamento_TestValidateDisponibilidadeForaDoHorarioDeTodasAsJanelasLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 19, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 20, 0);
        DisponibilidadeEntity disponibilidade = new DisponibilidadeEntity();
        disponibilidade.setDiaSemana(DiaSemana.QUINTA);
        disponibilidade.setHoraInicio(LocalTime.of(9, 0));
        disponibilidade.setHoraFim(LocalTime.of(18, 0));

        when(disponibilidadeService.findByDiaSemana(any())).thenReturn(List.of(disponibilidade));

        assertThatThrownBy(() -> validator.validateDisponibilidade(inicio, fim))
                .isInstanceOf(AgendamentoSemDisponibilidadeException.class);
    }

    /**
     * Verifica que um horário sem sobreposição com nenhum agendamento existente não lança exceção.
     */
    @Test
    void agendamento_TestValidateConflitoDeHorarioSemSobreposicaoNaoLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);

        when(repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                        eq(AgendamentoStatus.AGENDADO), any(), any()))
                .thenReturn(List.of());

        assertThatCode(() -> validator.validateConflitoDeHorario(null, inicio, fim))
                .doesNotThrowAnyException();
        verify(repository)
                .findByStatusAndDataGreaterThanEqualAndDataLessThan(eq(AgendamentoStatus.AGENDADO), any(), any());
    }

    /**
     * Verifica que um horário sobreposto a outro agendamento em {@code AGENDADO} lança
     * {@link AgendamentoConflitoException}.
     */
    @Test
    void agendamento_TestValidateConflitoDeHorarioComSobreposicaoLancaExcecao() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);
        AgendamentoEntity existente = new AgendamentoEntity();
        existente.setId(5L);
        existente.setData(LocalDateTime.of(2026, 9, 10, 10, 30));
        existente.setDuracao(30);

        when(repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                        eq(AgendamentoStatus.AGENDADO), any(), any()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> validator.validateConflitoDeHorario(null, inicio, fim))
                .isInstanceOf(AgendamentoConflitoException.class);
    }

    /**
     * Verifica que, em uma atualização, o próprio agendamento é ignorado na verificação de conflito mesmo
     * sobrepondo o horário informado.
     */
    @Test
    void agendamento_TestValidateConflitoDeHorarioNaAtualizacaoIgnoraOProprioAgendamento() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 10, 10, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 9, 10, 11, 0);
        AgendamentoEntity proprio = new AgendamentoEntity();
        proprio.setId(5L);
        proprio.setData(LocalDateTime.of(2026, 9, 10, 10, 0));
        proprio.setDuracao(60);

        when(repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                        eq(AgendamentoStatus.AGENDADO), any(), any()))
                .thenReturn(List.of(proprio));

        assertThatCode(() -> validator.validateConflitoDeHorario(5L, inicio, fim))
                .doesNotThrowAnyException();
    }
}
