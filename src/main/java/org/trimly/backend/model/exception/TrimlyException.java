package org.trimly.backend.model.exception;

/**
 * Raiz das exceções de regra de domínio do Trimly. Agrupa as falhas de validação e de estado das
 * entidades, separadas das falhas de autenticação da família {@code AuthException}.
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
