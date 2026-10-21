package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Horário sobreposto a outro agendamento `AGENDADO`.
 */
public class AgendamentoConflitoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoConflitoException(String message) {
        super(message);
    }
}
