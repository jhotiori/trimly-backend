package org.trimly.backend.model.exception.recesso;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Recesso inexistente.
 */
public class RecessoNaoEncontradoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public RecessoNaoEncontradoException() {
        super("Recesso não encontrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public RecessoNaoEncontradoException(String message) {
        super(message);
    }
}
