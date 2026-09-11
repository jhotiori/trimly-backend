package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Raiz das falhas de regra de serviço, inclusive as transições de status não permitidas.
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
