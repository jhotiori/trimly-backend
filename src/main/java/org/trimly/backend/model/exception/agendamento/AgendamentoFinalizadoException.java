package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Alteração de agendamento fora do status `AGENDADO`.
 */
public class AgendamentoFinalizadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoFinalizadoException() {
        super("Agendamento não pode ser alterado nesse status");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoFinalizadoException(String message) {
        super(message);
    }
}
