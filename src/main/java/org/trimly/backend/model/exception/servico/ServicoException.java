package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Falha genérica de regra de serviço.
 */
public class ServicoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public ServicoException(String message) {
        super(message);
    }
}
