package org.trimly.backend.config;

/**
 * Ponto único de definição dos caminhos base de cada controller REST.
 *
 * Cada constante guarda o literal exato usado no {@code @RequestMapping} do
 * controller
 * correspondente. Sub-caminhos de método continuam declarados nos próprios
 * controllers.
 */
public final class EndpointConfig {
    public static final String AGENDAMENTOS_ENDPOINT = "/api/agendamentos";
    public static final String SERVICOS_ENDPOINT = "/api/servicos";
    public static final String USUARIOS_ENDPOINT = "/api/usuarios";
    public static final String DISPONIBILIDADES_ENDPOINT = "/api/disponibilidades";
    public static final String AUTHENTICATION_ENDPOINT = "/api/auth";
    public static final String FRONTEND_ENDPOINT = "http://localhost:4200";

    private EndpointConfig() {}
}
