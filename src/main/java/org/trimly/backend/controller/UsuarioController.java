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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.infra.EndpointConfig;
import org.trimly.backend.infra.openapi.OpenApiExamples;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

/**
 * Controller de usuários, em `/api/usuarios`; o cadastro e o login ficam em `/api/auth`.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.USUARIOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Consulta, atualização e remoção de usuários")
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
     * Atualiza os campos informados de um usuário existente.
     *
     * @param id - identificador do usuário a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o usuário atualizado
     */
    @PreAuthorize("@accessPolicy.canUpdateUsuario(#id, #request)")
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza um usuário",
            description = "Nulos ignorados. E-mail único, senha mín. 6, cargo sem repetir nem tirar o DONO único."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Usuário atualizado (inalterado se tudo nulo)",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo, campos ou identificador inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.USUARIO_ATUALIZACAO_INVALIDA),
                    @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                    @ExampleObject(name = "Identificador inválido", value = OpenApiExamples.PARAMETRO_INVALIDO)
            })
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "E-mail já cadastrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_EMAIL_EXISTENTE)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Cargo repetido ou remoção do cargo do único DONO",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "Cargo repetido", value = OpenApiExamples.USUARIO_CARGO_REPETIDO),
                    @ExampleObject(name = "Único dono", value = OpenApiExamples.USUARIO_UNICO_DONO)
            })
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<UsuarioResponseDTO> update(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO request
    ) {
        log.debug(
                "update usuario: id={}, nome={}, email={}, cargo={}",
                id,
                request.nome(),
                request.email(),
                request.cargo()
        );

        UsuarioEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o usuário com o identificador informado.
     *
     * @param id - identificador do usuário a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @PreAuthorize("hasAnyRole('DONO', 'ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário", description = "Falha se houver agendamento em AGENDADO.")
    @ApiResponse(responseCode = "204", description = "Usuário removido", content = @Content)
    @ApiResponse(
            responseCode = "400",
            description = "Identificador não numérico",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Possui agendamento em AGENDADO",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_COM_AGENDAMENTO_PENDENTE)
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
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id
    ) {
        log.debug("delete usuario: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os usuários cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de usuários
     */
    @PreAuthorize("hasAnyRole('DONO', 'ADMIN')")

    @GetMapping
    @Operation(summary = "Lista usuários", description = "Não expõe senhas.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de usuários",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
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
    @PreAuthorize("@accessPolicy.isStaffOrSelf(#id)")
    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário")
    @ApiResponse(
            responseCode = "200",
            description = "Usuário encontrado",
            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Identificador não numérico",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuário não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.USUARIO_NAO_ENCONTRADO)
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
    public ResponseEntity<UsuarioResponseDTO> findById(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long id
    ) {
        UsuarioEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }
}
