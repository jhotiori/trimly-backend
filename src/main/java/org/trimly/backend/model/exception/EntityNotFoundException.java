package org.trimly.backend.model.exception;

/**
 * Entidade não encontrada.
 */
public class EntityNotFoundException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
