package org.trimly.backend.infra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.trimly.backend.infra.EndpointConfig;
import org.trimly.backend.infra.security.authentication.SecurityFilter;
import org.trimly.backend.infra.security.authorization.AccessPolicy;
import org.trimly.backend.model.service.auth.TokenService;
import org.trimly.backend.model.service.usuario.UsuarioService;

/**
 * Cadeia do Spring Security: sessão `STATELESS`, JWT via {@link SecurityFilter} e autenticação exigida fora das
 * rotas públicas.
 *
 * As regras de cargo e posse ficam nos métodos dos controllers (`@PreAuthorize` com `hasAnyRole` ou
 * {@link AccessPolicy}).
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    /**
     * Rotas liberadas sem token.
     */
    private static final String[] PUBLIC_ROUTES = {
            EndpointConfig.AUTHENTICATION_ENDPOINT + "/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error"
    };

    /**
     * Rota do console do H2, liberada apenas quando o perfil o habilita.
     */
    private static final String H2_CONSOLE_ROUTE = "/h2-console/**";

    /**
     * Geração e validação dos tokens JWT.
     * @see {@link TokenService}
     */
    private final TokenService tokenService;

    /**
     * Regras de negócio de usuários.
     * @see {@link UsuarioService}
     */
    private final UsuarioService usuarioService;

    /**
     * Respostas `401` e `403` da cadeia de filtros.
     * @see {@link SecurityErrorHandler}
     */
    private final SecurityErrorHandler errorHandler;

    /**
     * Indica se o console do H2 está habilitado no perfil ativo.
     */
    @Value("${spring.h2.console.enabled:false}")
    private boolean h2Console;

    /**
     * Monta a cadeia de filtros do Spring Security: CORS habilitado, CSRF desabilitado, sessão `STATELESS`, o
     * {@link SecurityFilter} de JWT antes do filtro de usuário e senha e autenticação exigida fora das rotas públicas.
     *
     * @param http - builder de configuração do Spring Security
     * @throws Exception - quando a montagem da cadeia de filtros falha
     * @return SecurityFilterChain - cadeia de filtros configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (h2Console) {
            http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        }

        return http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(PUBLIC_ROUTES).permitAll();
                    if (h2Console) {
                        auth.requestMatchers(H2_CONSOLE_ROUTE).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(
                        exceptions -> exceptions.authenticationEntryPoint(errorHandler)
                                .accessDeniedHandler(errorHandler)
                )
                .addFilterBefore(securityFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Cria o {@link SecurityFilter} da cadeia. Não é um `@Bean` para o container não registrá-lo como filtro fora da
     * cadeia do Spring Security.
     *
     * @return SecurityFilter - filtro que autentica a requisição pelo token `Bearer`
     */
    private SecurityFilter securityFilter() {
        return new SecurityFilter(tokenService, usuarioService);
    }
}
