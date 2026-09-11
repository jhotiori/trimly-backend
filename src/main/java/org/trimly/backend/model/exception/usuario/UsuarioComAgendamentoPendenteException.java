package org.trimly.backend.model.exception.usuario;

/**
 * Sinaliza a tentativa de remover um usuário que ainda tem agendamento em {@code AGENDADO}.
 */
public class UsuarioComAgendamentoPendenteException extends UsuarioException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public UsuarioComAgendamentoPendenteException(String message) {
        super(message);
    }
}
