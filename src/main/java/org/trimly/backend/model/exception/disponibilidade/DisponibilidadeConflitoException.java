package org.trimly.backend.model.exception.disponibilidade;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Janela sobreposta a outra do mesmo dia.
 */
public class DisponibilidadeConflitoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public DisponibilidadeConflitoException() {
        super("Horário já ocupado por outra disponibilidade nesse dia");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeConflitoException(String message) {
        super(message);
    }
}
