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

    /**
     * Cria a exceção com a mensagem e a causa informadas.
     *
     * @param message - detalhe da falha
     * @param cause - falha original preservada para o log
     */
    public TrimlyException(String message, Throwable cause) {
        super(message, cause);
    }
}
