package org.trimly.backend.model.exception.disponibilidade;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Janela sobreposta a outra do mesmo dia.
 */
public class DisponibilidadeConflitoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeConflitoException(String message) {
        super(message);
    }
}
