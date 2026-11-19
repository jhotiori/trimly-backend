package org.trimly.backend.model.exception.disponibilidade;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Hora de início não anterior à de fim.
 */
public class DisponibilidadeHorarioInvalidoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public DisponibilidadeHorarioInvalidoException() {
        super("Hora de início deve ser anterior à hora de fim");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeHorarioInvalidoException(String message) {
        super(message);
    }
}
