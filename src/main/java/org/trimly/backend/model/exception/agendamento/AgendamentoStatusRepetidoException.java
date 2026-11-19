package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Atualização para o status atual do agendamento.
 */
public class AgendamentoStatusRepetidoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoStatusRepetidoException() {
        super("Agendamento já está nesse status");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoStatusRepetidoException(String message) {
        super(message);
    }
}
