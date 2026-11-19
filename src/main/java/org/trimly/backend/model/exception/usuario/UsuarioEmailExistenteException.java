package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * E-mail já cadastrado para outro usuário.
 */
public class UsuarioEmailExistenteException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioEmailExistenteException() {
        super("E-mail já cadastrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioEmailExistenteException(String message) {
        super(message);
    }
}
