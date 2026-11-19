package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Remoção do cargo `DONO` do único dono cadastrado.
 */
public class UsuarioUnicoDonoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioUnicoDonoException() {
        super("Não é possível remover o cargo do único DONO");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioUnicoDonoException(String message) {
        super(message);
    }
}
