package org.trimly.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.config.openapi.OpenApiExamples;
import org.trimly.backend.model.service.auth.AuthenticationService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Controller para autenticação, com base em {@code /api/auth}, expondo o cadastro e o login que
 * devolvem um token JWT.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.AUTHENTICATION_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro e login via JWT (não utilizado pelo frontend atualmente)")
public class AuthenticationController {
    /**
     * Orquestração de cadastro e login.
     * @see {@link AuthenticationService}
     */
    private final AuthenticationService service;

    /**
     * Cadastra um novo usuário e retorna o token com status 201.
     *
     * @param request - dados do usuário a ser criado
     * @return ResponseEntity - resposta com o token do usuário recém-criado
     */
    @PostMapping("/register")
    @Operation(summary = "Cadastra um usuário e emite um token", description = """
            Cria um usuário CLIENTE (mesmas regras de POST /api/usuarios) e devolve um token JWT. Fluxo \
            independente do login de /api/usuarios/login, não usado pelo frontend hoje; nenhuma rota exige \
            token, por isso o Swagger UI não oferece autorização.""")
    @ApiResponse(
            responseCode = "201",
            description = "Usuário criado e token emitido",
            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.USUARIO_CRIACAO_INVALIDA),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "E-mail já cadastrado para outro usuário",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_EMAIL_EXISTENTE)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioCreateDTO request) {
        log.debug("register usuario: email={}", request.email());

        AuthResponseDTO response = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica o usuário e retorna o token com status 200.
     *
     * @param request - credenciais informadas
     * @return ResponseEntity - resposta com o token do usuário autenticado
     */
    @PostMapping("/login")
    @Operation(summary = "Autentica e emite um token", description = """
            Confere e-mail e senha e devolve um token JWT. Credenciais inválidas retornam 401 com mensagem \
            fixa, sem indicar qual campo falhou. Fluxo independente do login de /api/usuarios/login, não \
            usado pelo frontend hoje.""")
    @ApiResponse(
            responseCode = "200",
            description = "Credenciais conferem e o token foi emitido",
            content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.LOGIN_INVALIDO),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "E-mail inexistente ou senha incorreta, com mensagem fixa",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.CREDENCIAIS_INVALIDAS)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO request) {
        log.debug("login usuario: email={}", request.email());

        AuthResponseDTO response = service.login(request);
        return ResponseEntity.ok(response);
    }
}
