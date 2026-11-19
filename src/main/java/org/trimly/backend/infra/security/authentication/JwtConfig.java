package org.trimly.backend.infra.security.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de assinatura e verificação dos tokens JWT (HMAC256), criados uma única vez.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
    /**
     * Cria o algoritmo HMAC256 a partir do segredo configurado.
     *
     * @param secret - segredo HMAC usado para assinar e verificar os tokens
     * @return Algorithm - algoritmo usado para assinar e verificar os tokens
     */
    @Bean
    public Algorithm jwtAlgorithm(@Value("${trimly.security.jwt.secret}") String secret) {
        return Algorithm.HMAC256(secret);
    }

    /**
     * Cria o verificador que exige a assinatura e o emissor configurados.
     *
     * @param algorithm - algoritmo usado para verificar a assinatura
     * @param properties - propriedades dos tokens JWT
     * @return JWTVerifier - verificador dos tokens recebidos
     */
    @Bean
    public JWTVerifier jwtVerifier(Algorithm algorithm, JwtProperties properties) {
        return JWT.require(algorithm).withIssuer(properties.issuer()).build();
    }
}
