package org.trimly.backend.model.exception.disponibilidade;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Raiz das falhas de regra de disponibilidade.
 */
public class DisponibilidadeException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public DisponibilidadeException(String message) {
        super(message);
    }
}
