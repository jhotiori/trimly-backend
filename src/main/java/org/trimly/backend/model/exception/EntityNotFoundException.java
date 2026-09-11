package org.trimly.backend.model.exception;

/**
 * Sinaliza que a entidade buscada por identificador ou e-mail não existe.
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
