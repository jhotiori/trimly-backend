package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Remoção de usuário com agendamento `AGENDADO`.
 */
public class UsuarioComAgendamentoPendenteException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public UsuarioComAgendamentoPendenteException(String message) {
        super(message);
    }
}
