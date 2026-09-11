package org.trimly.backend.model.exception.disponibilidade;

/**
 * Sinaliza que a hora de início da disponibilidade não é anterior à hora de fim.
 */
public class DisponibilidadeHorarioInvalidoException extends DisponibilidadeException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeHorarioInvalidoException(String message) {
        super(message);
    }
}
