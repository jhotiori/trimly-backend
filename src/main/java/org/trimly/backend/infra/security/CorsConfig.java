package org.trimly.backend.infra.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Libera a origem do frontend (`trimly.cors.origin`) via CORS.
 */
@Configuration
public class CorsConfig {
    /**
     * Cria a fonte de configuração de CORS aplicada a todas as rotas, liberando a origem do frontend
     * para os métodos usados pela API.
     *
     * @param origin - origem do frontend liberada
     * @return CorsConfigurationSource - fonte de configuração de CORS registrada para todas as rotas
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${trimly.cors.origin}") String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
