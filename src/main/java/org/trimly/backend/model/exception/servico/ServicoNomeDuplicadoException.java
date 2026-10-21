package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Nome de serviço já usado por outro.
 */
public class ServicoNomeDuplicadoException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public ServicoNomeDuplicadoException(String message) {
        super(message);
    }
}
