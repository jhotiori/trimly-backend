package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Agendamento com início e fim em dias diferentes.
 */
public class AgendamentoEntreDiasException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoEntreDiasException() {
        super("Agendamento deve começar e terminar no mesmo dia");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoEntreDiasException(String message) {
        super(message);
    }
}
