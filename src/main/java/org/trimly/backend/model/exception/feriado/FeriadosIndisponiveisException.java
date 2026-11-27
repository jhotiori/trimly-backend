package org.trimly.backend.model.exception.feriado;

import org.trimly.backend.model.exception.TrimlyException;

/**
 * API de feriados nacionais indisponível ou com resposta inválida.
 */
public class FeriadosIndisponiveisException extends TrimlyException {
    /**
     * Mensagem fixa devolvida ao cliente.
     */
    private static final String MENSAGEM = "Feriados indisponíveis no momento";

    /**
     * Cria a exceção com a mensagem padrão, preservando a falha da integração.
     *
     * @param cause - falha original da chamada à BrasilAPI
     */
    public FeriadosIndisponiveisException(Throwable cause) {
        super(MENSAGEM, cause);
    }
}
