package org.trimly.backend.infra.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.trimly.backend.controller.AuthenticationController;
import org.trimly.backend.infra.security.SecurityErrorHandler;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Metadados do OpenAPI exibido em `/swagger-ui.html`, com o esquema `bearerAuth` nas rotas protegidas.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Nome do esquema de segurança JWT.
     */
    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Define o título, a descrição, a versão da API e o esquema `bearerAuth`.
     *
     * @return OpenAPI - documento base com os metadados da API
     */
    @Bean
    public OpenAPI openApi() {
        SecurityScheme bearer = new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        return new OpenAPI()
                .info(new Info().title("API Trimly").description("Agendamentos da barbearia Trimly").version("0.0.1"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, bearer));
    }

    /**
     * Marca as rotas fora de `/api/auth` com `bearerAuth` e a resposta `401`, e as rotas com `@PreAuthorize`
     * com a resposta `403`.
     *
     * @return OperationCustomizer - customizador aplicado a cada operação
     */
    @Bean
    public OperationCustomizer securityResponses() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.getBeanType().equals(AuthenticationController.class)) {
                return operation;
            }

            operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
            operation.getResponses()
                    .addApiResponse(
                            "401",
                            errorResponse(
                                    HttpStatus.UNAUTHORIZED,
                                    "Token ausente ou inválido",
                                    SecurityErrorHandler.NAO_AUTENTICADO
                            )
                    );

            if (handlerMethod.hasMethodAnnotation(PreAuthorize.class)) {
                operation.getResponses()
                        .addApiResponse(
                                "403",
                                errorResponse(
                                        HttpStatus.FORBIDDEN,
                                        "Sem permissão para o recurso",
                                        SecurityErrorHandler.ACESSO_NEGADO
                                )
                        );
            }
            return operation;
        };
    }

    /**
     * Monta a resposta de erro documentada com o corpo `ErrorResponseDTO` de exemplo.
     *
     * @param status - status HTTP da resposta
     * @param description - descrição da resposta
     * @param message - mensagem fixa do erro
     * @return ApiResponse - resposta documentada
     */
    private ApiResponse errorResponse(HttpStatus status, String description, String message) {
        MediaType media = new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponseDTO"))
                .addExamples("default", new Example().value(ErrorResponseDTO.of(status, message)));
        return new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json", media));
    }
}
