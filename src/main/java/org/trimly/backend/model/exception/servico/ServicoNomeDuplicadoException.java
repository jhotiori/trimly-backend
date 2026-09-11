package org.trimly.backend.model.exception.servico;

/**
 * Sinaliza a tentativa de criar um serviço com um nome já usado por outro.
 */
public class ServicoNomeDuplicadoException extends ServicoException {
    /**
     * Cria a exceção com a mensagem informada.
     *
     * @param message - detalhe da falha
     */
    public ServicoNomeDuplicadoException(String message) {
        super(message);
    }
}
