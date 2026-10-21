package org.trimly.backend.config;

/**
 * Caminhos base dos controllers REST e origem do frontend.
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
     * Caminho base do controller de autenticação.
     */
    public static final String AUTHENTICATION_ENDPOINT = "/api/auth";

    /**
     * Origem do frontend, liberada em CorsConfig.
     */
    public static final String FRONTEND_ENDPOINT = "http://localhost:4200";

    private EndpointConfig() {}
}
