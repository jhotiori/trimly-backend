package org.trimly.backend.model.exception;

/**
 * Raiz das exceções de regra de domínio.
 */
public class TrimlyException extends RuntimeException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public TrimlyException(String message) {
        super(message);
    }
}
