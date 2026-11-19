package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Início do agendamento no passado.
 */
public class AgendamentoNoPassadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoNoPassadoException() {
        super("Agendamento não pode ser no passado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoNoPassadoException(String message) {
        super(message);
    }
}
