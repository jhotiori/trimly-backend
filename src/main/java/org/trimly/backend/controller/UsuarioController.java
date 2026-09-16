package org.trimly.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.config.openapi.OpenApiExamples;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioLoginRequestDTO;
import org.trimly.backend.view.dto.usuario.UsuarioLoginResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

/**
 * Controller para usuários, com base em {@code /api/usuarios}. Expõe o cadastro público e um login
 * por credenciais que não emite token.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.USUARIOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro e consulta de usuários")
public class UsuarioController {
    /**
     * Regras de negócio de usuários.
     * @see {@link UsuarioService}
     */
    private final UsuarioService service;

    /**
     * Mapper de usuários.
     * @see {@link UsuarioMapper}
     */
    private final UsuarioMapper mapper;

    /**
     * Cria um novo usuário com cargo cliente.
     *
     * @param request - dados do usuário a ser criado
     * @return ResponseEntity - resposta com o usuário criado
     */
    @PostMapping
    @Operation(
            summary = "Cria um usuário",
            description = "Cadastra um usuário sempre com cargo CLIENTE. O e-mail deve ser único e a senha, com no"
                    + " mínimo 6 caracteres, é armazenada criptografada; a resposta nunca expõe a senha.")
    @ApiResponse(
            responseCode = "201",
            description = "Usuário criado",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description =
                    "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                @ExampleObject(
                                        name = "Campos inválidos",
                                        value = OpenApiExamples.USUARIO_CRIACAO_INVALIDA),
                                @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)
                            }))
    @ApiResponse(
            responseCode = "409",
            description = "E-mail já cadastrado para outro usuário",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_EMAIL_EXISTENTE)))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioCreateDTO request) {
        log.debug("create usuario: nome={}, email={}", request.nome(), request.email());

        UsuarioEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Autentica um usuário pelas credenciais informadas. Responde sempre {@code 200}: o campo
     * {@code sucesso} indica o resultado e {@code usuario} é nulo quando as credenciais não conferem.
     *
     * @param request - credenciais informadas no login
     * @return ResponseEntity - resposta com o resultado da autenticação
     */
    @PostMapping("/login")
    @Operation(
            summary = "Autentica um usuário por credenciais",
            description = "Confere e-mail e senha sem emitir token. A resposta é sempre 200, inclusive quando as"
                    + " credenciais não conferem: nesse caso sucesso vem false e usuario vem nulo, sem indicar se o"
                    + " e-mail ou a senha falhou. Este login nunca retorna 401.")
    @ApiResponse(
            responseCode = "200",
            description = "Resultado da autenticação; sucesso indica se as credenciais conferem",
            content =
                    @Content(
                            schema = @Schema(implementation = UsuarioLoginResponseDTO.class),
                            examples = {
                                @ExampleObject(name = "Credenciais válidas", value = """
                                        {"sucesso": true, "usuario": {"id": 1, "nome": "Administrador", \
                                        "email": "admin@trimly.com", "cargo": "ADMIN"}}"""),
                                @ExampleObject(name = "Credenciais inválidas", value = """
                                        {"sucesso": false, "usuario": null}""")
                            }))
    @ApiResponse(
            responseCode = "400",
            description =
                    "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.LOGIN_INVALIDO),
                                @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)
                            }))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<UsuarioLoginResponseDTO> login(@Valid @RequestBody UsuarioLoginRequestDTO request) {
        log.debug("login usuario: email={}", request.email());

        Optional<UsuarioEntity> usuario = service.findByCredenciais(request.email(), request.senha());
        if (usuario.isEmpty()) {
            return ResponseEntity.ok(new UsuarioLoginResponseDTO(false, null));
        }

        return ResponseEntity.ok(new UsuarioLoginResponseDTO(true, mapper.toResponse(usuario.get())));
    }

    /**
     * Atualiza os campos informados de um usuário existente.
     *
     * @param id - identificador do usuário a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o usuário atualizado
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza um usuário",
            description = "Atualiza os campos informados. Campos nulos ou em branco são ignorados; com todos nulos,"
                    + " devolve o usuário sem alterações. O novo e-mail deve ser único, a nova senha deve ter no mínimo"
                    + " 6 caracteres e é recriptografada,"
                    + " o novo cargo não pode repetir o atual e o único DONO cadastrado não pode perder o cargo.")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado, ou inalterado quando todos os campos são nulos",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Corpo mal formatado, campos inválidos ou identificador não numérico",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                @ExampleObject(
                                        name = "Campos inválidos",
                                        value = OpenApiExamples.USUARIO_ATUALIZACAO_INVALIDA),
                                @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                                @ExampleObject(
                                        name = "Identificador inválido",
                                        value = OpenApiExamples.PARAMETRO_INVALIDO)
                            }))
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(
            responseCode = "409",
            description = "Novo e-mail já cadastrado para outro usuário",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_EMAIL_EXISTENTE)))
    @ApiResponse(
            responseCode = "422",
            description = "Cargo repetido ou remoção do cargo do único DONO",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = {
                                @ExampleObject(name = "Cargo repetido", value = OpenApiExamples.USUARIO_CARGO_REPETIDO),
                                @ExampleObject(name = "Único dono", value = OpenApiExamples.USUARIO_UNICO_DONO)
                            }))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO request) {
        log.debug(
                "update usuario: id={}, nome={}, email={}, cargo={}",
                id,
                request.nome(),
                request.email(),
                request.cargo());

        UsuarioEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista todos os usuários cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de usuários
     */
    @GetMapping
    @Operation(summary = "Lista usuários", description = "Lista todos os usuários cadastrados, sem expor senhas.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários, possivelmente vazia",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class))))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        List<UsuarioEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um usuário pelo seu identificador.
     *
     * @param id - identificador do usuário
     * @return ResponseEntity - resposta com o usuário encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário", description = "Busca um usuário pelo identificador.")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Identificador não numérico",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)))
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<UsuarioResponseDTO> findById(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id) {
        UsuarioEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o usuário com o identificador informado.
     *
     * @param id - identificador do usuário a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remove um usuário",
            description = "Remove o usuário com o identificador informado, desde que não tenha agendamento em"
                    + " AGENDADO vinculado.")
    @ApiResponse(responseCode = "204", description = "Usuário removido", content = @Content)
    @ApiResponse(
            responseCode = "400",
            description = "Identificador não numérico",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)))
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)))
    @ApiResponse(
            responseCode = "409",
            description = "Usuário com agendamento em AGENDADO vinculado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.USUARIO_COM_AGENDAMENTO_PENDENTE)))
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content =
                    @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class),
                            examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)))
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id) {
        log.debug("delete usuario: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
