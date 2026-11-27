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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.infra.EndpointConfig;
import org.trimly.backend.infra.openapi.OpenApiExamples;
import org.trimly.backend.model.entity.recesso.RecessoEntity;
import org.trimly.backend.model.service.recesso.RecessoService;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.dto.recesso.RecessoCreateDTO;
import org.trimly.backend.view.dto.recesso.RecessoResponseDTO;
import org.trimly.backend.view.dto.recesso.RecessoUpdateDTO;
import org.trimly.backend.view.mapper.RecessoMapper;

/**
 * Controller de recessos, em `/api/recessos`.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.RECESSOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Recessos", description = "Gestão dos períodos em que a barbearia não atende")
public class RecessoController {
    /**
     * Regras de negócio de recessos.
     * @see {@link RecessoService}
     */
    private final RecessoService service;

    /**
     * Mapper de recessos.
     * @see {@link RecessoMapper}
     */
    private final RecessoMapper mapper;

    /**
     * Cria um recesso e retorna o recurso criado com status 201.
     *
     * @param request - dados do recesso a ser criado
     * @return ResponseEntity - resposta com o recesso criado
     */
    @PreAuthorize("hasAnyRole('DONO', 'ADMIN')")
    @PostMapping
    @Operation(
            summary = "Cria um recesso",
            description = "Início hoje ou depois, fim não anterior ao início, sem sobreposição com outro recesso."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Recesso criado",
            content = @Content(schema = @Schema(implementation = RecessoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.RECESSO_CRIACAO_INVALIDA),
                    @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)
            })
    )
    @ApiResponse(
            responseCode = "409",
            description = "Sobreposição com outro recesso",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.RECESSO_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Início no passado ou fim anterior ao início",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "No passado", value = OpenApiExamples.RECESSO_NO_PASSADO),
                    @ExampleObject(name = "Período inválido", value = OpenApiExamples.RECESSO_PERIODO_INVALIDO)
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
    public ResponseEntity<RecessoResponseDTO> create(@Valid @RequestBody RecessoCreateDTO request) {
        log.debug("create recesso: dataInicio={}, dataFim={}", request.dataInicio(), request.dataFim());

        RecessoEntity entity = service.create(request);
        log.info("recesso criado: dataInicio={}, dataFim={}", request.dataInicio(), request.dataFim());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza os campos informados de um recesso existente.
     *
     * @param id - identificador do recesso a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o recesso atualizado
     */
    @PreAuthorize("hasAnyRole('DONO', 'ADMIN')")
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza um recesso",
            description = "Nulos ignorados; datas enviadas não podem estar no passado, valem as demais regras da criação."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Recesso atualizado (inalterado se tudo nulo)",
            content = @Content(schema = @Schema(implementation = RecessoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ou identificador inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                    @ExampleObject(name = "Identificador inválido", value = OpenApiExamples.PARAMETRO_INVALIDO)
            })
    )
    @ApiResponse(
            responseCode = "404",
            description = "Recesso não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.RECESSO_NAO_ENCONTRADO)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Sobreposição com outro recesso",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.RECESSO_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Data enviada no passado ou fim anterior ao início",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class), examples = {
                    @ExampleObject(name = "No passado", value = OpenApiExamples.RECESSO_NO_PASSADO),
                    @ExampleObject(name = "Período inválido", value = OpenApiExamples.RECESSO_PERIODO_INVALIDO)
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
    public ResponseEntity<RecessoResponseDTO> update(
            @Parameter(description = "Identificador do recesso", example = "1") @PathVariable Long id,
            @Valid @RequestBody RecessoUpdateDTO request
    ) {
        log.debug("update recesso: id={}, dataInicio={}, dataFim={}", id, request.dataInicio(), request.dataFim());

        RecessoEntity entity = service.update(id, request);
        log.info("recesso atualizado: id={}", id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o recesso com o identificador informado.
     *
     * @param id - identificador do recesso a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @PreAuthorize("hasAnyRole('DONO', 'ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um recesso")
    @ApiResponse(responseCode = "204", description = "Recesso removido", content = @Content)
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
            description = "Recesso não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.RECESSO_NAO_ENCONTRADO)
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
            @Parameter(description = "Identificador do recesso", example = "1") @PathVariable Long id
    ) {
        log.debug("delete recesso: id={}", id);

        service.deleteById(id);
        log.info("recesso removido: id={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os recessos cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de recessos
     */
    @GetMapping
    @Operation(summary = "Lista recessos")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de recessos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecessoResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<List<RecessoResponseDTO>> findAll() {
        log.debug("find recessos");

        List<RecessoEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um recesso pelo seu identificador.
     *
     * @param id - identificador do recesso
     * @return ResponseEntity - resposta com o recesso encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca um recesso")
    @ApiResponse(
            responseCode = "200",
            description = "Recesso encontrado",
            content = @Content(schema = @Schema(implementation = RecessoResponseDTO.class))
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
            description = "Recesso não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.RECESSO_NAO_ENCONTRADO)
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
    public ResponseEntity<RecessoResponseDTO> findById(
            @Parameter(description = "Identificador do recesso", example = "1") @PathVariable Long id
    ) {
        log.debug("find recesso: id={}", id);

        RecessoEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }
}
