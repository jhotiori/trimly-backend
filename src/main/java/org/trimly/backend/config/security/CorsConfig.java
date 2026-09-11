package org.trimly.backend.config.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.trimly.backend.config.EndpointConfig;

/**
 * Fornece o {@link CorsConfigurationSource} que libera a origem do frontend
 * ({@code EndpointConfig.FRONTEND_ENDPOINT}) para os métodos usados pela API.
 */
@Configuration
public class CorsConfig {
    /**
     * Cria a fonte de configuração de CORS aplicada a todas as rotas, liberando a origem do frontend
     * para os métodos usados pela API.
     *
     * @return CorsConfigurationSource - fonte de configuração de CORS registrada para todas as rotas
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(EndpointConfig.FRONTEND_ENDPOINT));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
