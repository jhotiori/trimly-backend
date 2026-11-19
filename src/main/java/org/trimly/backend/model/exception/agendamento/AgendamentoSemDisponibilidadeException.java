package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Nenhuma disponibilidade comporta o horário do agendamento.
 */
public class AgendamentoSemDisponibilidadeException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoSemDisponibilidadeException() {
        super("Não há disponibilidade nesse dia");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoSemDisponibilidadeException(String message) {
        super(message);
    }
}
