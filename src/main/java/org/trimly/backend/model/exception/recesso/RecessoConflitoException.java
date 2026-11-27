package org.trimly.backend.model.exception.recesso;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Recesso sobreposto a outro.
 */
public class RecessoConflitoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public RecessoConflitoException() {
        super("Já existe um recesso nesse período");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public RecessoConflitoException(String message) {
        super(message);
    }
}
