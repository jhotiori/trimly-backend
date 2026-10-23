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
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeResponseDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

/**
 * Controller de disponibilidades, em `/api/disponibilidades`.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.DISPONIBILIDADES_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Disponibilidades", description = "Gestão das janelas de atendimento da barbearia")
public class DisponibilidadeController {
    /**
     * Regras de negócio de disponibilidades.
     * @see {@link DisponibilidadeService}
     */
    private final DisponibilidadeService service;

    /**
     * Mapper de disponibilidades.
     * @see {@link DisponibilidadeMapper}
     */
    private final DisponibilidadeMapper mapper;

    /**
     * Cria uma disponibilidade e retorna o recurso criado com status 201.
     *
     * @param request - dados da disponibilidade a ser criada
     * @return ResponseEntity - resposta com a disponibilidade criada
     */
    @PostMapping
    @Operation(
            summary = "Cria uma disponibilidade",
            description = "Início antes do fim, sem sobreposição no dia; janelas que só se tocam não conflitam."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Disponibilidade criada",
            content = @Content(schema = @Schema(implementation = DisponibilidadeResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou inválido",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "Campos inválidos",
                                    value = OpenApiExamples.DISPONIBILIDADE_CRIACAO_INVALIDA
                            ),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Sobreposição no mesmo dia",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Início não anterior ao fim",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_HORARIO_INVALIDO)
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
    public ResponseEntity<DisponibilidadeResponseDTO> create(@Valid @RequestBody DisponibilidadeCreateDTO request) {
        log.debug(
                "create disponibilidade: diaSemana={}, horaInicio={}, horaFim={}",
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim()
        );

        DisponibilidadeEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza os campos informados de uma disponibilidade existente.
     *
     * @param id - identificador da disponibilidade a ser atualizada
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com a disponibilidade atualizada
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza uma disponibilidade",
            description = "Nulos ignorados; valem as regras da criação, sem conflito com a própria janela."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Disponibilidade atualizada (inalterada se tudo nula)",
            content = @Content(schema = @Schema(implementation = DisponibilidadeResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ou identificador inválido",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                            @ExampleObject(name = "Identificador inválido", value = OpenApiExamples.PARAMETRO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Disponibilidade não encontrada",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_NAO_ENCONTRADA)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Sobreposição no mesmo dia",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Início não anterior ao fim",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_HORARIO_INVALIDO)
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
    public ResponseEntity<DisponibilidadeResponseDTO> update(
            @Parameter(description = "Identificador da disponibilidade", example = "1") @PathVariable Long id,
            @Valid @RequestBody DisponibilidadeUpdateDTO request
    ) {
        log.debug(
                "update disponibilidade: id={}, diaSemana={}, horaInicio={}, horaFim={}",
                id,
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim()
        );

        DisponibilidadeEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove a disponibilidade com o identificador informado.
     *
     * @param id - identificador da disponibilidade a ser removida
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma disponibilidade")
    @ApiResponse(responseCode = "204", description = "Disponibilidade removida", content = @Content)
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
            description = "Disponibilidade não encontrada",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_NAO_ENCONTRADA)
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
            @Parameter(description = "Identificador da disponibilidade", example = "1") @PathVariable Long id
    ) {
        log.debug("delete disponibilidade: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todas as disponibilidades cadastradas.
     *
     * @return ResponseEntity - resposta com a lista de disponibilidades
     */
    @GetMapping
    @Operation(summary = "Lista disponibilidades")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de disponibilidades",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DisponibilidadeResponseDTO.class))
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
    public ResponseEntity<List<DisponibilidadeResponseDTO>> findAll() {
        List<DisponibilidadeEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca uma disponibilidade pelo seu identificador.
     *
     * @param id - identificador da disponibilidade
     * @return ResponseEntity - resposta com a disponibilidade encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca uma disponibilidade")
    @ApiResponse(
            responseCode = "200",
            description = "Disponibilidade encontrada",
            content = @Content(schema = @Schema(implementation = DisponibilidadeResponseDTO.class))
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
            description = "Disponibilidade não encontrada",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.DISPONIBILIDADE_NAO_ENCONTRADA)
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
    public ResponseEntity<DisponibilidadeResponseDTO> findById(
            @Parameter(description = "Identificador da disponibilidade", example = "1") @PathVariable Long id
    ) {
        DisponibilidadeEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista as disponibilidades cadastradas para o dia da semana informado.
     *
     * @param diaSemana - dia da semana usado no filtro
     * @return ResponseEntity - resposta com a lista de disponibilidades do dia
     */
    @GetMapping("/dia/{diaSemana}")
    @Operation(
            summary = "Lista disponibilidades de um dia da semana",
            description = "Aceita SEGUNDA a DOMINGO em qualquer caixa; dia inválido retorna 500."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de disponibilidades do dia",
            content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DisponibilidadeResponseDTO.class))
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Dia inválido ou erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<List<DisponibilidadeResponseDTO>> findByDiaSemana(
            @Parameter(
                    description = "Dia da semana, de SEGUNDA a DOMINGO",
                    example = "SEGUNDA"
            ) @PathVariable String diaSemana
    ) {
        List<DisponibilidadeEntity> entities = service.findByDiaSemana(DiaSemana.fromString(diaSemana));
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }
}
