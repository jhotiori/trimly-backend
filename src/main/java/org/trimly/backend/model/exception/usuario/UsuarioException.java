package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Raiz das falhas de regra de usuário, inclusive as reatribuições de cargo não permitidas.
 */
public class UsuarioException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public UsuarioException(String message) {
        super(message);
    }
}
