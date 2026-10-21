package org.trimly.backend.model.exception.agendamento;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Data do agendamento é feriado nacional.
 */
public class AgendamentoFeriadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public AgendamentoFeriadoException(String message) {
        super(message);
    }
}
