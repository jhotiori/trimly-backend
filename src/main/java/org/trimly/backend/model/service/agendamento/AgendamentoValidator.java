package org.trimly.backend.model.service.agendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.agendamento.AgendamentoConflitoException;
import org.trimly.backend.model.exception.agendamento.AgendamentoForaDoHorarioException;
import org.trimly.backend.model.exception.agendamento.AgendamentoSemDisponibilidadeException;
import org.trimly.backend.model.exception.agendamento.AgendamentoStatusException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;

/**
 * Validador de agendamentos. Aplica, nesta ordem, a legalidade da transição de status, o limite de um
 * dia entre início e fim, o encaixe em uma janela de disponibilidade e a ausência de conflito com
 * outros agendamentos em {@code AGENDADO}.
 */
@Component
@RequiredArgsConstructor
public class AgendamentoValidator {
    /**
     * Repositório de agendamentos, usado para checar conflitos de horário.
     * @see {@link AgendamentoRepository}
     */
    private final AgendamentoRepository repository;

    /**
     * Serviço de disponibilidades, usado para validar as janelas de atendimento do dia.
     * @see {@link DisponibilidadeService}
     */
    private final DisponibilidadeService disponibilidadeService;

    /**
     * Verifica se o agendamento pode ser alterado e, quando um novo status é informado, se a transição
     * solicitada é permitida pelo ciclo de vida.
     *
     * Um agendamento só pode ser alterado - em qualquer campo, inclusive o status - enquanto estiver em
     * {@code AGENDADO}. Como {@code AGENDADO} é o único status não terminal, exigir que a origem seja
     * {@code AGENDADO} e o destino seja diferente já cobre toda a legalidade da transição.
     *
     * @param agendamento - agendamento a ser alterado
     * @param novo - status desejado, ou {@code null} quando o status não está sendo alterado
     * @throws AgendamentoStatusException - quando o agendamento não está em {@code AGENDADO}, ou quando o novo status é igual ao atual
     */
    public void validateStatusUpdate(AgendamentoEntity agendamento, AgendamentoStatus novo) {
        if (agendamento.getStatus() != AgendamentoStatus.AGENDADO) {
            throw new AgendamentoStatusException("O agendamento não pode ser alterado neste status");
        }

        if (novo != null && novo == agendamento.getStatus()) {
            throw new AgendamentoStatusException("O agendamento já está nesse status");
        }
    }

    /**
     * Verifica se o início e o fim do agendamento caem no mesmo dia.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @param fimAgendamento - data e hora de fim do agendamento
     * @throws AgendamentoForaDoHorarioException - quando o agendamento passa de um dia para o outro
     */
    public void validateHorarioFuturo(LocalDateTime inicioAgendamento, LocalDateTime fimAgendamento) {
        if (!inicioAgendamento.toLocalDate().equals(fimAgendamento.toLocalDate())) {
            throw new AgendamentoForaDoHorarioException(
                    "O agendamento não pode ultrapassar o horário de um dia para o outro");
        }
    }

    /**
     * Verifica se o agendamento cabe inteiramente em alguma disponibilidade do dia da semana.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @param fimAgendamento - data e hora de fim do agendamento
     * @throws AgendamentoSemDisponibilidadeException - quando não há disponibilidade que comporte o horário
     */
    public void validateDisponibilidade(LocalDateTime inicioAgendamento, LocalDateTime fimAgendamento) {
        DiaSemana diaSemanaAgendamento = DiaSemana.fromDayOfWeek(inicioAgendamento.getDayOfWeek());
        List<DisponibilidadeEntity> disponibilidades = disponibilidadeService.findByDiaSemana(diaSemanaAgendamento);

        if (disponibilidades.isEmpty()) {
            throw new AgendamentoSemDisponibilidadeException(
                    "Não há nenhuma disponibilidade nesse dia para realizar este agendamento");
        }

        LocalTime horaInicioAgendamento = inicioAgendamento.toLocalTime();
        LocalTime horaFimAgendamento = fimAgendamento.toLocalTime();

        boolean estaDentroDeUmaDisponibilidade = false;
        for (DisponibilidadeEntity disponibilidade : disponibilidades) {
            LocalTime horaInicioDisponibilidade = disponibilidade.getHoraInicio();
            LocalTime horaFimDisponibilidade = disponibilidade.getHoraFim();

            boolean inicioEstaDentro = !horaInicioAgendamento.isBefore(horaInicioDisponibilidade);
            boolean fimEstaDentro = !horaFimAgendamento.isAfter(horaFimDisponibilidade);

            if (inicioEstaDentro && fimEstaDentro) {
                estaDentroDeUmaDisponibilidade = true;
                break;
            }
        }

        if (!estaDentroDeUmaDisponibilidade) {
            throw new AgendamentoSemDisponibilidadeException(
                    "Não há disponibilidade suficiente nesse dia para realizar este agendamento");
        }
    }

    /**
     * Verifica se o horário informado se sobrepõe a outro agendamento ativo no mesmo dia.
     *
     * @param id - identificador do agendamento a ignorar na verificação, ou nulo na criação
     * @param inicioNovoAgendamento - data e hora de início do novo agendamento
     * @param fimNovoAgendamento - data e hora de fim do novo agendamento
     * @throws AgendamentoConflitoException - quando o horário conflita com outro agendamento
     */
    public void validateConflitoDeHorario(
            Long id, LocalDateTime inicioNovoAgendamento, LocalDateTime fimNovoAgendamento) {
        LocalDate dataAgendamento = inicioNovoAgendamento.toLocalDate();
        LocalDateTime inicioDoDia = dataAgendamento.atStartOfDay();
        LocalDateTime inicioDoProximoDia = dataAgendamento.plusDays(1).atStartOfDay();
        List<AgendamentoEntity> agendamentosExistentes = repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                AgendamentoStatus.AGENDADO, inicioDoDia, inicioDoProximoDia);

        for (AgendamentoEntity agendamentoExistente : agendamentosExistentes) {
            // Durante um update, ignora o próprio agendamento.
            if (id != null && agendamentoExistente.getId().equals(id)) {
                continue;
            }

            LocalDateTime inicioAgendamentoExistente = agendamentoExistente.getData();
            LocalDateTime fimAgendamentoExistente =
                    inicioAgendamentoExistente.plusMinutes(agendamentoExistente.getDuracao());

            boolean inicioExistenteAntesDoFimNovo = inicioAgendamentoExistente.isBefore(fimNovoAgendamento);
            boolean fimExistenteDepoisDoInicioNovo = fimAgendamentoExistente.isAfter(inicioNovoAgendamento);
            boolean existeConflito = inicioExistenteAntesDoFimNovo && fimExistenteDepoisDoInicioNovo;

            if (existeConflito) {
                throw new AgendamentoConflitoException("O horário escolhido já está ocupado por outro agendamento");
            }
        }
    }
}
