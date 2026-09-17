package org.trimly.backend.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados do documento OpenAPI exibido pelo Swagger UI em {@code /swagger-ui.html}.
 *
 * Não declara esquema de segurança de propósito: nenhuma rota exige autenticação hoje, então o
 * Swagger UI não oferece autorização por token.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Define o título, a descrição e a versão da API.
     *
     * @return OpenAPI - documento base com os metadados da API
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(
                new Info().title("API Trimly")
                        .description(
                                "API de agendamentos da barbearia Trimly: usuários, serviços, disponibilidades e"
                                        + " agendamentos."
                        )
                        .version("0.0.1")
        );
    }
}
