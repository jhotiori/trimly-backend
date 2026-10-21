package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Agendamento com início e fim em dias diferentes.
 */
public class AgendamentoForaDoHorarioException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoForaDoHorarioException(String message) {
        super(message);
    }
}
