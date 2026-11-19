package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Agendamento que não cabe inteiro em uma janela de disponibilidade do dia.
 */
public class AgendamentoForaDaDisponibilidadeException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoForaDaDisponibilidadeException() {
        super("Horário fora da disponibilidade do dia");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoForaDaDisponibilidadeException(String message) {
        super(message);
    }
}
