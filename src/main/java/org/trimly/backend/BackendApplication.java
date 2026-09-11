package org.trimly.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Spring Boot do Trimly.
 */
@SpringBootApplication
public class BackendApplication {
    /**
     * Sobe o contexto Spring Boot da aplicação.
     *
     * @param args - argumentos de linha de comando repassados ao Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
