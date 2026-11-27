package org.trimly.backend.model.exception.recesso;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Fim do recesso anterior ao início.
 */
public class RecessoPeriodoInvalidoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public RecessoPeriodoInvalidoException() {
        super("Data de fim deve ser igual ou posterior à data de início");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public RecessoPeriodoInvalidoException(String message) {
        super(message);
    }
}
