package org.trimly.backend.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;

/**
 * Repositório de agendamentos, com filtragem dinâmica via {@link JpaSpecificationExecutor}.
 */
public interface AgendamentoRepository
        extends
            JpaRepository<AgendamentoEntity, Long>,
            JpaSpecificationExecutor<AgendamentoEntity> {
    /**
     * Retorna os agendamentos com o status informado cuja data esteja no período delimitado.
     *
     * @param status - status usado no filtro
     * @param inicio - início do período, inclusivo
     * @param fim - fim do período, exclusivo
     * @return List - agendamentos que atendem ao status e ao período, vazia quando não houver nenhum
     */
    List<AgendamentoEntity> findByStatusAndDataGreaterThanEqualAndDataLessThan(
            AgendamentoStatus status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    /**
     * Indica se existe algum agendamento do serviço informado, com o status informado, marcado após a data de
     * referência.
     *
     * @param servicoId - identificador do serviço
     * @param status - status usado no filtro
     * @param referencia - data e hora a partir da qual o agendamento deve ocorrer, exclusiva
     * @return boolean - `true` quando há ao menos um agendamento nessas condições
     */
    boolean existsByServicoIdAndStatusAndDataAfter(Long servicoId, AgendamentoStatus status, LocalDateTime referencia);

    /**
     * Indica se existe algum agendamento do usuário informado com o status informado.
     *
     * @param usuarioId - identificador do usuário
     * @param status - status usado no filtro
     * @return boolean - `true` quando há ao menos um agendamento nessas condições
     */
    boolean existsByUsuarioIdAndStatus(Long usuarioId, AgendamentoStatus status);

    /**
     * Indica se existe algum agendamento do serviço informado com o status informado.
     *
     * @param servicoId - identificador do serviço
     * @param status - status usado no filtro
     * @return boolean - `true` quando há ao menos um agendamento nessas condições
     */
    boolean existsByServicoIdAndStatus(Long servicoId, AgendamentoStatus status);
}
