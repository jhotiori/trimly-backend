package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Data do agendamento dentro de um recesso.
 */
public class AgendamentoRecessoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoRecessoException() {
        super("Dia selecionado está em recesso");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoRecessoException(String message) {
        super(message);
    }
}
