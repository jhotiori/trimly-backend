package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Nenhuma disponibilidade comporta o horário do agendamento.
 */
public class AgendamentoSemDisponibilidadeException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoSemDisponibilidadeException(String message) {
        super(message);
    }
}
