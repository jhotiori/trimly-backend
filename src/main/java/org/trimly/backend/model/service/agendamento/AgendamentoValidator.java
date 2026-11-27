package org.trimly.backend.model.service.agendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeDiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
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
 * Valida status, horário, antecedência, feriado, recesso, disponibilidade e conflito de agendamentos.
 */
@Component
@RequiredArgsConstructor
public class AgendamentoValidator {
    /**
     * Quantidade máxima de dias entre o momento atual e o início do agendamento.
     */
    private static final int LIMITE_ANTECEDENCIA_DIAS = 14;

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
     * Cliente da API de feriados nacionais (BrasilAPI), usado para bloquear agendamentos em datas de feriado.
     * @see {@link FeriadosClient}
     */
    private final FeriadosClient feriadosClient;

    /**
     * Repositório de recessos, usado para bloquear agendamentos em datas de recesso.
     * @see {@link RecessoRepository}
     */
    private final RecessoRepository recessoRepository;

    /**
     * Verifica se o agendamento pode ser alterado e, quando um novo status é informado, se a transição
     * solicitada é permitida pelo ciclo de vida.
     *
     * Um agendamento só pode ser alterado - em qualquer campo, inclusive o status - enquanto estiver em
     * `AGENDADO`. Como `AGENDADO` é o único status não terminal, exigir que a origem seja
     * `AGENDADO` e o destino seja diferente já cobre toda a legalidade da transição.
     *
     * @param agendamento - agendamento a ser alterado
     * @param novo - status desejado, ou `null` quando o status não está sendo alterado
     * @throws AgendamentoFinalizadoException - quando o agendamento não está em `AGENDADO`
     * @throws AgendamentoStatusRepetidoException - quando o novo status é igual ao atual
     */
    public void validateStatusUpdate(AgendamentoEntity agendamento, AgendamentoStatus novo) {
        if (agendamento.getStatus() != AgendamentoStatus.AGENDADO) {
            throw new AgendamentoFinalizadoException();
        }

        if (novo == agendamento.getStatus()) {
            throw new AgendamentoStatusRepetidoException();
        }
    }

    /**
     * Verifica se o início do agendamento ainda não passou e se início e fim caem no mesmo dia.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @param fimAgendamento - data e hora de fim do agendamento
     * @throws AgendamentoNoPassadoException - quando o início do agendamento já passou
     * @throws AgendamentoEntreDiasException - quando o agendamento passa de um dia para o outro
     */
    public void validateHorarioFuturo(LocalDateTime inicioAgendamento, LocalDateTime fimAgendamento) {
        if (inicioAgendamento.isBefore(LocalDateTime.now())) {
            throw new AgendamentoNoPassadoException();
        }

        if (!inicioAgendamento.toLocalDate().equals(fimAgendamento.toLocalDate())) {
            throw new AgendamentoEntreDiasException();
        }
    }

    /**
     * Verifica se o início do agendamento não ultrapassa o limite de 14 dias a partir do momento atual.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @throws AgendamentoAntecedenciaExcedidaException - quando o início é posterior a 14 dias a partir de agora
     */
    public void validateLimiteAntecedencia(LocalDateTime inicioAgendamento) {
        if (inicioAgendamento.isAfter(LocalDateTime.now().plusDays(LIMITE_ANTECEDENCIA_DIAS))) {
            throw new AgendamentoAntecedenciaExcedidaException();
        }
    }

    /**
     * Verifica se o agendamento cabe inteiramente em alguma disponibilidade do dia da semana.
     *
     * @param inicioAgendamento - data e hora de início do agendamento
     * @param fimAgendamento - data e hora de fim do agendamento
     * @throws AgendamentoSemDisponibilidadeException - quando o dia não tem disponibilidade
     * @throws AgendamentoForaDaDisponibilidadeException - quando o horário não cabe em uma janela do dia
     */
    public void validateDisponibilidade(LocalDateTime inicioAgendamento, LocalDateTime fimAgendamento) {
        DisponibilidadeDiaSemana diaSemanaAgendamento = DisponibilidadeDiaSemana
                .fromDayOfWeek(inicioAgendamento.getDayOfWeek());
        List<DisponibilidadeEntity> disponibilidades = disponibilidadeService.findByDiaSemana(diaSemanaAgendamento);

        if (disponibilidades.isEmpty()) {
            throw new AgendamentoSemDisponibilidadeException();
        }

        LocalTime horaInicioAgendamento = inicioAgendamento.toLocalTime();
        LocalTime horaFimAgendamento = fimAgendamento.toLocalTime();

        boolean estaDentroDeUmaDisponibilidade = disponibilidades.stream()
                .anyMatch(
                        disponibilidade -> !horaInicioAgendamento.isBefore(disponibilidade.getHoraInicio())
                                && !horaFimAgendamento.isAfter(disponibilidade.getHoraFim())
                );

        if (!estaDentroDeUmaDisponibilidade) {
            throw new AgendamentoForaDaDisponibilidadeException();
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
            Long id,
            LocalDateTime inicioNovoAgendamento,
            LocalDateTime fimNovoAgendamento
    ) {
        LocalDate dataAgendamento = inicioNovoAgendamento.toLocalDate();
        LocalDateTime inicioDoDia = dataAgendamento.atStartOfDay();
        LocalDateTime inicioDoProximoDia = dataAgendamento.plusDays(1).atStartOfDay();
        List<AgendamentoEntity> agendamentosExistentes = repository.findByStatusAndDataGreaterThanEqualAndDataLessThan(
                AgendamentoStatus.AGENDADO,
                inicioDoDia,
                inicioDoProximoDia
        );

        boolean existeConflito = agendamentosExistentes.stream()
                .filter(agendamentoExistente -> !agendamentoExistente.getId().equals(id))
                .anyMatch(agendamentoExistente -> {
                    LocalDateTime inicioExistente = agendamentoExistente.getData();
                    LocalDateTime fimExistente = inicioExistente.plusMinutes(agendamentoExistente.getDuracao());
                    return inicioExistente.isBefore(fimNovoAgendamento) && fimExistente.isAfter(inicioNovoAgendamento);
                });

        if (existeConflito) {
            throw new AgendamentoConflitoException();
        }
    }

    /**
     * Verifica se está tentando agendar para um feriado.
     *
     * @param data - data de um agendamento
     * @throws AgendamentoFeriadoException - quando a data coincide com um feriado nacional
     */
    public void validateIsFeriado(LocalDate data) {
        List<FeriadoResponseDTO> feriados = feriadosClient.listarPorAno(data.getYear());
        boolean isFeriado = feriados.stream().map(f -> LocalDate.parse(f.date())).anyMatch(data::isEqual);

        if (isFeriado) {
            throw new AgendamentoFeriadoException();
        }
    }

    /**
     * Verifica se está tentando agendar para um dia dentro de um recesso, com as duas pontas inclusivas.
     *
     * @param data - data de um agendamento
     * @throws AgendamentoRecessoException - quando a data cai dentro de um recesso
     */
    public void validateIsRecesso(LocalDate data) {
        if (!recessoRepository.findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(data, data).isEmpty()) {
            throw new AgendamentoRecessoException();
        }
    }
}
