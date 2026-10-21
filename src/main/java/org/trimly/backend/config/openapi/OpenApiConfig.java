package org.trimly.backend.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadados do OpenAPI exibido em `/swagger-ui.html`, sem esquema de segurança.
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
        return new OpenAPI()
                .info(new Info().title("API Trimly").description("Agendamentos da barbearia Trimly").version("0.0.1"));
    }
}
