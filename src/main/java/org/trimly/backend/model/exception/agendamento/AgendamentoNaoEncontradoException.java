package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Agendamento inexistente.
 */
public class AgendamentoNaoEncontradoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoNaoEncontradoException() {
        super("Agendamento não encontrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoNaoEncontradoException(String message) {
        super(message);
    }
}
