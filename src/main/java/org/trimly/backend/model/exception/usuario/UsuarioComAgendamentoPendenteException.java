package org.trimly.backend.model.exception.usuario;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Remoção de usuário com agendamento `AGENDADO`.
 */
public class UsuarioComAgendamentoPendenteException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public UsuarioComAgendamentoPendenteException() {
        super("Não é possível remover usuário com agendamento pendente");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public UsuarioComAgendamentoPendenteException(String message) {
        super(message);
    }
}
