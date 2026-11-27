package org.trimly.backend.model.exception.recesso;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Data do recesso no passado.
 */
public class RecessoNoPassadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public RecessoNoPassadoException() {
        super("Recesso não pode ser no passado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public RecessoNoPassadoException(String message) {
        super(message);
    }
}
