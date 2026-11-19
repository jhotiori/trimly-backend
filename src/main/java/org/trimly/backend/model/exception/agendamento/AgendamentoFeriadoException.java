package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Data do agendamento é feriado nacional.
 */
public class AgendamentoFeriadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public AgendamentoFeriadoException() {
        super("Dia selecionado é feriado nacional");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoFeriadoException(String message) {
        super(message);
    }
}
