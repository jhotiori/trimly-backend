package org.trimly.backend.model.entity.agendamento;

/**
 * Status do ciclo de vida de um agendamento. {@code AGENDADO} é o único estado não terminal, do qual
 * o agendamento pode seguir para {@code CANCELADO}, {@code CONCLUIDO} ou {@code AUSENTE}, todos finais.
 */
public enum AgendamentoStatus {
    AGENDADO,
    CANCELADO,
    CONCLUIDO,
    AUSENTE
}
