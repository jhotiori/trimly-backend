package org.trimly.backend.model.entity.agendamento;

/**
 * Status do agendamento; `AGENDADO` é o único não terminal.
 */
public enum AgendamentoStatus {
    AGENDADO, CANCELADO, CONCLUIDO, AUSENTE
}
