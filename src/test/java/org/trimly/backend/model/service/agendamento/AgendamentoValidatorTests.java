package org.trimly.backend.model.service.agendamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeDiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.exception.agendamento.AgendamentoAntecedenciaExcedidaException;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoEntreDiasException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFeriadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoFinalizadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoForaDaDisponibilidadeException;
import org.trimly.backend.model.exception.agendamento.AgendamentoNoPassadoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoRecessoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoSemDisponibilidadeException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusRepetidoException;
import org.trimly.backend.model.integration.feriados.FeriadoResponseDTO;
import org.trimly.backend.model.integration.feriados.FeriadosClient;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.RecessoRepository;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;

/**
 * Testes para {@link AgendamentoValidator}.
 */
@ExtendWith(MockitoExtension.class)
class AgendamentoValidatorTests {
    /**
     * Segunda-feira base dos cenários.
     */
    private static final LocalDate SEGUNDA = LocalDate.of(2030, 1, 7);

    /**
     * Repositório de agendamentos simulado.
     * @see {@link AgendamentoRepository}
     */
    @Mock
    private AgendamentoRepository repository;

    /**
     * Serviço de disponibilidades simulado.
     * @see {@link DisponibilidadeService}
     */
    @Mock
    private DisponibilidadeService disponibilidadeService;

    /**
     * Cliente de feriados simulado.
     * @see {@link FeriadosClient}
     */
    @Mock
    private FeriadosClient feriadosClient;

    /**
     * Repositório de recessos simulado.
     * @see {@link RecessoRepository}
     */
    @Mock
    private RecessoRepository recessoRepository;

    /**
     * Validador sob teste, com os mocks injetados.
     * @see {@link AgendamentoValidator}
     */
    @InjectMocks
    private AgendamentoValidator validator;

    @Test
    @DisplayName("Aceita mudar um agendamento AGENDADO para outro status")
    void testeDadoAgendamentoAgendadoQuandoValidarStatusParaCanceladoEntaoNaoLanca() {
        AgendamentoEntity agendamento = agendamento(1L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO);

        assertDoesNotThrow(() -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.CANCELADO));
    }

    @Test
    @DisplayName("Aceita atualização sem novo status")
    void testeDadoStatusNuloQuandoValidarStatusEntaoNaoLanca() {
        AgendamentoEntity agendamento = agendamento(1L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO);

        assertDoesNotThrow(() -> validator.validateStatusUpdate(agendamento, null));
    }

    @Test
    @DisplayName("Rejeita alterar agendamento em status terminal")
    void testeDadoAgendamentoCanceladoQuandoValidarStatusEntaoLancaStatusTerminal() {
        AgendamentoEntity agendamento = agendamento(1L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.CANCELADO);

        AgendamentoFinalizadoException excecao = assertThrows(
                AgendamentoFinalizadoException.class,
                () -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.CONCLUIDO)
        );

        assertEquals("Agendamento não pode ser alterado nesse status", excecao.getMessage());
    }

    @Test
    @DisplayName("Rejeita repetir o status atual")
    void testeDadoMesmoStatusQuandoValidarStatusEntaoLancaStatusRepetido() {
        AgendamentoEntity agendamento = agendamento(1L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO);

        AgendamentoStatusRepetidoException excecao = assertThrows(
                AgendamentoStatusRepetidoException.class,
                () -> validator.validateStatusUpdate(agendamento, AgendamentoStatus.AGENDADO)
        );

        assertEquals("Agendamento já está nesse status", excecao.getMessage());
    }

    @Test
    @DisplayName("Rejeita início no passado")
    void testeDadoInicioNoPassadoQuandoValidarHorarioFuturoEntaoLancaNoPassado() {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(1);
        LocalDateTime fim = inicio.plusMinutes(30);

        assertThrows(AgendamentoNoPassadoException.class, () -> validator.validateHorarioFuturo(inicio, fim));
    }

    @Test
    @DisplayName("Rejeita agendamento que atravessa para o dia seguinte")
    void testeDadoFimEmOutroDiaQuandoValidarHorarioFuturoEntaoLancaEntreDias() {
        LocalDateTime inicio = LocalDate.now().plusDays(2).atTime(23, 30);
        LocalDateTime fim = inicio.plusMinutes(60);

        assertThrows(AgendamentoEntreDiasException.class, () -> validator.validateHorarioFuturo(inicio, fim));
    }

    @Test
    @DisplayName("Aceita início futuro que termina no mesmo dia")
    void testeDadoInicioFuturoNoMesmoDiaQuandoValidarHorarioFuturoEntaoNaoLanca() {
        LocalDateTime inicio = LocalDate.now().plusDays(2).atTime(9, 0);

        assertDoesNotThrow(() -> validator.validateHorarioFuturo(inicio, inicio.plusMinutes(30)));
    }

    @Test
    @DisplayName("Aceita início exatamente 14 dias à frente")
    void testeDadoInicioEm14DiasQuandoValidarLimiteEntaoNaoLanca() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(14);

        assertDoesNotThrow(() -> validator.validateLimiteAntecedencia(inicio));
    }

    @Test
    @DisplayName("Rejeita início além de 14 dias")
    void testeDadoInicioAlemDe14DiasQuandoValidarLimiteEntaoLancaAntecedenciaExcedida() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(14).plusMinutes(1);

        assertThrows(AgendamentoAntecedenciaExcedidaException.class, () -> validator.validateLimiteAntecedencia(inicio));
    }

    @Test
    @DisplayName("Rejeita dia sem nenhuma disponibilidade")
    void testeDadoDiaSemDisponibilidadeQuandoValidarDisponibilidadeEntaoLancaSemDisponibilidade() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA)).thenReturn(List.of());

        LocalDateTime inicio = SEGUNDA.atTime(9, 0);
        LocalDateTime fim = SEGUNDA.atTime(9, 30);

        AgendamentoSemDisponibilidadeException excecao = assertThrows(
                AgendamentoSemDisponibilidadeException.class,
                () -> validator.validateDisponibilidade(inicio, fim)
        );

        assertEquals("Não há disponibilidade nesse dia", excecao.getMessage());
    }

    @Test
    @DisplayName("Aceita agendamento dentro de uma janela")
    void testeDadoAgendamentoDentroDaJanelaQuandoValidarDisponibilidadeEntaoNaoLanca() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA)).thenReturn(List.of(janela(7, 12)));

        assertDoesNotThrow(() -> validator.validateDisponibilidade(SEGUNDA.atTime(9, 0), SEGUNDA.atTime(9, 30)));
    }

    @Test
    @DisplayName("Aceita agendamento que ocupa a janela inteira")
    void testeDadoAgendamentoNosLimitesDaJanelaQuandoValidarDisponibilidadeEntaoNaoLanca() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA)).thenReturn(List.of(janela(7, 12)));

        assertDoesNotThrow(() -> validator.validateDisponibilidade(SEGUNDA.atTime(7, 0), SEGUNDA.atTime(12, 0)));
    }

    @Test
    @DisplayName("Aceita agendamento que cabe na segunda janela do dia")
    void testeDadoDuasJanelasQuandoValidarDisponibilidadeEntaoAceitaAgendamentoNaSegunda() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA))
                .thenReturn(List.of(janela(7, 12), janela(13, 18)));

        assertDoesNotThrow(() -> validator.validateDisponibilidade(SEGUNDA.atTime(14, 0), SEGUNDA.atTime(14, 30)));
    }

    @Test
    @DisplayName("Rejeita agendamento que ultrapassa o fim da janela")
    void testeDadoAgendamentoQueUltrapassaAJanelaQuandoValidarDisponibilidadeEntaoLancaForaDaDisponibilidade() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA)).thenReturn(List.of(janela(7, 12)));

        LocalDateTime inicio = SEGUNDA.atTime(11, 45);
        LocalDateTime fim = SEGUNDA.atTime(12, 15);

        AgendamentoForaDaDisponibilidadeException excecao = assertThrows(
                AgendamentoForaDaDisponibilidadeException.class,
                () -> validator.validateDisponibilidade(inicio, fim)
        );

        assertEquals("Horário fora da disponibilidade do dia", excecao.getMessage());
    }

    @Test
    @DisplayName("Rejeita agendamento que atravessa o intervalo entre janelas")
    void testeDadoAgendamentoSobreOIntervaloQuandoValidarDisponibilidadeEntaoLancaForaDaDisponibilidade() {
        when(disponibilidadeService.findByDiaSemana(DisponibilidadeDiaSemana.SEGUNDA))
                .thenReturn(List.of(janela(7, 12), janela(13, 18)));

        LocalDateTime inicio = SEGUNDA.atTime(11, 45);
        LocalDateTime fim = SEGUNDA.atTime(13, 15);

        assertThrows(
                AgendamentoForaDaDisponibilidadeException.class,
                () -> validator.validateDisponibilidade(inicio, fim)
        );
    }

    @Test
    @DisplayName("Aceita horário quando não há agendamentos no dia")
    void testeDadoDiaSemAgendamentosQuandoValidarConflitoEntaoNaoLanca() {
        buscarAgendadosDoDia();

        assertDoesNotThrow(
                () -> validator.validateConflitoDeHorario(null, SEGUNDA.atTime(9, 0), SEGUNDA.atTime(9, 30))
        );
    }

    @Test
    @DisplayName("Rejeita horário que sobrepõe parcialmente outro agendamento")
    void testeDadoSobreposicaoParcialQuandoValidarConflitoEntaoLancaConflito() {
        buscarAgendadosDoDia(agendamento(10L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO));

        LocalDateTime inicio = SEGUNDA.atTime(9, 15);
        LocalDateTime fim = SEGUNDA.atTime(9, 45);

        assertThrows(AgendamentoConflitoException.class, () -> validator.validateConflitoDeHorario(null, inicio, fim));
    }

    @Test
    @DisplayName("Rejeita horário que engloba outro agendamento")
    void testeDadoHorarioQueEnglobaOutroQuandoValidarConflitoEntaoLancaConflito() {
        buscarAgendadosDoDia(agendamento(10L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO));

        LocalDateTime inicio = SEGUNDA.atTime(8, 0);
        LocalDateTime fim = SEGUNDA.atTime(11, 0);

        assertThrows(AgendamentoConflitoException.class, () -> validator.validateConflitoDeHorario(null, inicio, fim));
    }

    @Test
    @DisplayName("Aceita horários que apenas encostam em outro agendamento")
    void testeDadoHorariosAdjacentesQuandoValidarConflitoEntaoNaoLanca() {
        buscarAgendadosDoDia(agendamento(10L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO));

        assertAll(
                () -> assertDoesNotThrow(
                        () -> validator.validateConflitoDeHorario(null, SEGUNDA.atTime(8, 30), SEGUNDA.atTime(9, 0))
                ),
                () -> assertDoesNotThrow(
                        () -> validator.validateConflitoDeHorario(null, SEGUNDA.atTime(9, 30), SEGUNDA.atTime(10, 0))
                )
        );
    }

    @Test
    @DisplayName("Ignora o próprio agendamento ao atualizar")
    void testeDadoMesmoIdQuandoValidarConflitoEntaoIgnoraOProprioAgendamento() {
        buscarAgendadosDoDia(agendamento(10L, SEGUNDA.atTime(9, 0), 30, AgendamentoStatus.AGENDADO));

        assertDoesNotThrow(() -> validator.validateConflitoDeHorario(10L, SEGUNDA.atTime(9, 0), SEGUNDA.atTime(9, 30)));
    }

    @Test
    @DisplayName("Rejeita data que é feriado nacional")
    void testeDadoDataDeFeriadoQuandoValidarFeriadoEntaoLancaFeriado() {
        when(feriadosClient.listarPorAno(2030)).thenReturn(feriadosDe2030());

        LocalDate feriado = LocalDate.of(2030, 1, 1);

        assertThrows(AgendamentoFeriadoException.class, () -> validator.validateIsFeriado(feriado));
    }

    @Test
    @DisplayName("Aceita data que não é feriado")
    void testeDadoDataComumQuandoValidarFeriadoEntaoNaoLanca() {
        when(feriadosClient.listarPorAno(2030)).thenReturn(feriadosDe2030());

        assertDoesNotThrow(() -> validator.validateIsFeriado(SEGUNDA));
    }

    @Test
    @DisplayName("Rejeita data dentro de um recesso, incluindo o primeiro e o último dia")
    void testeDadoDataDentroDoRecessoQuandoValidarRecessoEntaoLancaRecesso() {
        LocalDate inicio = LocalDate.of(2030, 1, 7);
        LocalDate fim = LocalDate.of(2030, 1, 10);
        List<RecessoEntity> recessos = List.of(recesso(inicio, fim));
        when(recessoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(inicio, inicio))
                .thenReturn(recessos);
        when(recessoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(fim, fim))
                .thenReturn(recessos);

        assertAll(
                () -> assertThrows(AgendamentoRecessoException.class, () -> validator.validateIsRecesso(inicio)),
                () -> assertThrows(AgendamentoRecessoException.class, () -> validator.validateIsRecesso(fim))
        );
    }

    @Test
    @DisplayName("Aceita data um dia antes do início e um dia depois do fim do recesso")
    void testeDadoDataForaDoRecessoQuandoValidarRecessoEntaoNaoLanca() {
        LocalDate diaAntes = LocalDate.of(2030, 1, 6);
        LocalDate diaDepois = LocalDate.of(2030, 1, 11);
        when(recessoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(diaAntes, diaAntes))
                .thenReturn(List.of());
        when(recessoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(diaDepois, diaDepois))
                .thenReturn(List.of());

        assertAll(
                () -> assertDoesNotThrow(() -> validator.validateIsRecesso(diaAntes)),
                () -> assertDoesNotThrow(() -> validator.validateIsRecesso(diaDepois))
        );
    }

    private void buscarAgendadosDoDia(AgendamentoEntity... agendamentos) {
        when(
                repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                        AgendamentoStatus.AGENDADO,
                        SEGUNDA.atStartOfDay(),
                        SEGUNDA.plusDays(1).atStartOfDay()
                )
        ).thenReturn(List.of(agendamentos));
    }

    private static RecessoEntity recesso(LocalDate dataInicio, LocalDate dataFim) {
        return RecessoEntity.builder().descricao("Férias").dataInicio(dataInicio).dataFim(dataFim).build();
    }

    private static List<FeriadoResponseDTO> feriadosDe2030() {
        return List.of(new FeriadoResponseDTO("2030-01-01", "Confraternização Universal", "national"));
    }

    private static DisponibilidadeEntity janela(int horaInicio, int horaFim) {
        return DisponibilidadeEntity.builder()
                .diaSemana(DisponibilidadeDiaSemana.SEGUNDA)
                .horaInicio(LocalTime.of(horaInicio, 0))
                .horaFim(LocalTime.of(horaFim, 0))
                .build();
    }

    private static AgendamentoEntity agendamento(Long id, LocalDateTime data, int duracao, AgendamentoStatus status) {
        return AgendamentoEntity.builder().id(id).data(data).duracao(duracao).status(status).build();
    }
}
