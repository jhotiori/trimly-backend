package org.trimly.backend.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Fornece o bean {@link PasswordEncoder} baseado em BCrypt, usado para cifrar e conferir senhas.
 */
@Configuration
public class PasswordConfig {
    /**
     * Cria o {@link PasswordEncoder} baseado em BCrypt usado para cifrar e conferir senhas.
     *
     * @return PasswordEncoder - codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
