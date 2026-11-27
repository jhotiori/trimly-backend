package org.trimly.backend.infra;

/**
 * Caminhos base dos controllers REST.
 */
public final class EndpointConfig {
    /**
     * Caminho base do controller de agendamentos.
     */
    public static final String AGENDAMENTOS_ENDPOINT = "/api/agendamentos";

    /**
     * Caminho base do controller de serviços.
     */
    public static final String SERVICOS_ENDPOINT = "/api/servicos";

    /**
     * Caminho base do controller de usuários.
     */
    public static final String USUARIOS_ENDPOINT = "/api/usuarios";

    /**
     * Caminho base do controller de disponibilidades.
     */
    public static final String DISPONIBILIDADES_ENDPOINT = "/api/disponibilidades";

    /**
     * Caminho base do controller de recessos.
     */
    public static final String RECESSOS_ENDPOINT = "/api/recessos";

    /**
     * Caminho base do controller de feriados.
     */
    public static final String FERIADOS_ENDPOINT = "/api/feriados";

    /**
     * Caminho base do controller de autenticação.
     */
    public static final String AUTHENTICATION_ENDPOINT = "/api/auth";

    private EndpointConfig() {}
}
