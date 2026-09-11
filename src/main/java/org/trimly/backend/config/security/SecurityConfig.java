package org.trimly.backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.trimly.backend.model.service.auth.TokenService;
import org.trimly.backend.model.service.usuario.UsuarioService;

/**
 * Monta a cadeia de filtros do Spring Security: CSRF desabilitado, sessão {@code STATELESS} e o
 * {@link SecurityFilter} de JWT antes do filtro de usuário e senha. A autorização ainda é
 * {@code permitAll} em todas as rotas.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
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
     * Monta a cadeia de filtros do Spring Security: CORS habilitado, CSRF desabilitado, sessão
     * {@code STATELESS} e o {@link SecurityFilter} de JWT antes do filtro de usuário e senha. Todas
     * as rotas ficam abertas: a autorização é {@code permitAll} em qualquer requisição.
     *
     * @param http - builder de configuração do Spring Security
     * @throws Exception - quando a montagem da cadeia de filtros falha
     * @return SecurityFilterChain - cadeia de filtros configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(
                        new SecurityFilter(tokenService, usuarioService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
