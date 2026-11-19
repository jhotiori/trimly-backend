package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Usuário inexistente.
 */
public class UsuarioNaoEncontradoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioNaoEncontradoException(String message) {
        super(message);
    }
}
