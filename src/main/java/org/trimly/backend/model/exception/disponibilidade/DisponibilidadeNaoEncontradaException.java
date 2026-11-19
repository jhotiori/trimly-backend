package org.trimly.backend.model.exception.disponibilidade;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Disponibilidade inexistente.
 */
public class DisponibilidadeNaoEncontradaException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public DisponibilidadeNaoEncontradaException() {
        super("Disponibilidade não encontrada");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeNaoEncontradaException(String message) {
        super(message);
    }
}
