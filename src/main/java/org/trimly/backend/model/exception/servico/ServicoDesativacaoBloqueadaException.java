package org.trimly.backend.model.exception.servico;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * Desativação de serviço com agendamento `AGENDADO`.
 */
public class ServicoDesativacaoBloqueadaException extends TrimlyException {
    /**
     * Cria a exceção com a mensagem padrão.
     */
    public ServicoDesativacaoBloqueadaException() {
        super("Não é possível desativar serviço com agendamento pendente");
    }

    /**
     * Cria a exceção com uma mensagem própria.
     *
     * @param message - detalhe da falha
     */
    public ServicoDesativacaoBloqueadaException(String message) {
        super(message);
    }
}
