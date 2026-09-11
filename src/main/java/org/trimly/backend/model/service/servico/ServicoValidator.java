package org.trimly.backend.model.service.servico;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.exception.servico.ServicoComAgendamentoPendenteException;
import org.trimly.backend.model.exception.servico.ServicoException;
import org.trimly.backend.model.exception.servico.ServicoNomeDuplicadoException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.ServicoRepository;

/**
 * Validador de serviços. Garante a unicidade do nome, a legalidade da transição de status, a proibição
 * de desativar um serviço com agendamentos futuros e a ausência de agendamento pendente na remoção.
 */
@Component
@RequiredArgsConstructor
public class ServicoValidator {
    /**
     * Repositório de serviços, usado para checar a unicidade do nome.
     * @see {@link ServicoRepository}
     */
    private final ServicoRepository repository;

    /**
     * Repositório de agendamentos, usado para checar agendamentos pendentes do serviço.
     * @see {@link AgendamentoRepository}
     */
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Verifica se ainda não existe um serviço com o nome informado.
     *
     * @param nome - nome do serviço a ser verificado
     * @throws ServicoNomeDuplicadoException - quando já existe um serviço com esse nome
     */
    public void validateNomeUnico(String nome) {
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ServicoNomeDuplicadoException("Já existe um serviço com esse nome");
        }
    }

    /**
     * Verifica se a transição de status solicitada para o serviço é permitida.
     *
     * @param servico - serviço a ter o status alterado
     * @param novo - status desejado, ou {@code null} quando o status não está sendo alterado
     * @throws ServicoException - quando o novo status é igual ao atual, ou quando a desativação é solicitada e o serviço ainda tem agendamentos futuros em {@code AGENDADO}
     */
    public void validateStatusUpdate(ServicoEntity servico, ServicoStatus novo) {
        if (novo == null) {
            return;
        }

        if (servico.getStatus() == novo) {
            throw new ServicoException("O serviço já está nesse status");
        }

        if (novo == ServicoStatus.INATIVO
                && agendamentoRepository.existsByServicoIdAndStatusAndDataAfter(
                        servico.getId(), AgendamentoStatus.AGENDADO, LocalDateTime.now())) {
            throw new ServicoException("O serviço não pode ser desativado enquanto tiver agendamentos futuros");
        }
    }

    /**
     * Verifica se o serviço não possui agendamento pendente.
     *
     * @param servicoId - identificador do serviço a ser removido
     * @throws ServicoComAgendamentoPendenteException - quando o serviço possui agendamento em {@code AGENDADO}
     */
    public void validateSemAgendamentoPendente(Long servicoId) {
        if (agendamentoRepository.existsByServicoIdAndStatus(servicoId, AgendamentoStatus.AGENDADO)) {
            throw new ServicoComAgendamentoPendenteException(
                    "Não é possível remover um serviço com agendamento pendente");
        }
    }
}
