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
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoResponseDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

/**
 * Controller para serviços, com base em {@code /api/servicos}.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.SERVICOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Gestão dos serviços oferecidos pela barbearia")
public class ServicoController {
    /**
     * Regras de negócio de serviços.
     * @see {@link ServicoService}
     */
    private final ServicoService service;

    /**
     * Mapper de serviços.
     * @see {@link ServicoMapper}
     */
    private final ServicoMapper mapper;

    /**
     * Cria um serviço e retorna o recurso criado com status 201.
     *
     * @param request - dados do serviço a ser criado
     * @return ResponseEntity - resposta com o serviço criado
     */
    @PostMapping
    @Operation(
            summary = "Cria um serviço",
            description = "Cria um serviço no catálogo com status ATIVO. O nome deve ser único entre os serviços."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Serviço criado",
            content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Campos inválidos", value = OpenApiExamples.SERVICO_CRIACAO_INVALIDA),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Nome já usado por outro serviço",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_NOME_DUPLICADO)
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
    public ResponseEntity<ServicoResponseDTO> create(@Valid @RequestBody ServicoCreateDTO request) {
        log.debug("create servico: nome={}, valor={}, duracao={}", request.nome(), request.valor(), request.duracao());

        ServicoEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza os campos informados de um serviço existente.
     *
     * @param id - identificador do serviço a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o serviço atualizado
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza um serviço",
            description = "Atualiza os campos informados. Campos nulos ou em branco são ignorados; com todos nulos,"
                    + " devolve o serviço sem alterações. O novo nome deve ser único, o novo status não pode repetir o"
                    + " atual, e o serviço não pode ser desativado enquanto tiver agendamentos futuros."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Serviço atualizado, ou inalterado quando todos os campos são nulos",
            content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo mal formatado, campos inválidos ou identificador não numérico",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "Campos inválidos",
                                    value = OpenApiExamples.SERVICO_ATUALIZACAO_INVALIDA
                            ),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                            @ExampleObject(name = "Identificador inválido", value = OpenApiExamples.PARAMETRO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Serviço não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_NAO_ENCONTRADO)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Novo nome já usado por outro serviço",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_NOME_DUPLICADO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Status repetido ou desativação de serviço com agendamentos futuros",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Status repetido", value = OpenApiExamples.SERVICO_STATUS_REPETIDO),
                            @ExampleObject(
                                    name = "Desativação com agendamentos",
                                    value = OpenApiExamples.SERVICO_DESATIVACAO_COM_AGENDAMENTO
                            )}
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
    public ResponseEntity<ServicoResponseDTO> update(
            @Parameter(description = "Identificador do serviço", example = "1") @PathVariable Long id,
            @Valid @RequestBody ServicoUpdateDTO request
    ) {
        log.debug(
                "update servico: id={}, nome={}, valor={}, duracao={}, status={}",
                id,
                request.nome(),
                request.valor(),
                request.duracao(),
                request.status()
        );

        ServicoEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista todos os serviços cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de serviços
     */
    @GetMapping
    @Operation(summary = "Lista serviços", description = "Lista todos os serviços do catálogo, ativos e inativos.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de serviços, possivelmente vazia",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicoResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Erro inesperado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.ERRO_INESPERADO)
            )
    )
    public ResponseEntity<List<ServicoResponseDTO>> findAll() {
        List<ServicoEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um serviço pelo seu identificador.
     *
     * @param id - identificador do serviço
     * @return ResponseEntity - resposta com o serviço encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca um serviço", description = "Busca um serviço pelo identificador.")
    @ApiResponse(
            responseCode = "200",
            description = "Serviço encontrado",
            content = @Content(schema = @Schema(implementation = ServicoResponseDTO.class))
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
            description = "Serviço não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_NAO_ENCONTRADO)
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
    public ResponseEntity<ServicoResponseDTO> findById(
            @Parameter(description = "Identificador do serviço", example = "1") @PathVariable Long id
    ) {
        ServicoEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista os serviços com o status informado.
     *
     * @param status - status usado no filtro
     * @return ResponseEntity - resposta com a lista de serviços
     */
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Lista serviços por status",
            description = "Lista os serviços do catálogo com o status informado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de serviços com o status, possivelmente vazia",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServicoResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Status que não corresponde a nenhum valor do enum",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)
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
    public ResponseEntity<List<ServicoResponseDTO>> findByStatus(
            @Parameter(
                    description = "Status do serviço no catálogo",
                    example = "ATIVO"
            ) @PathVariable ServicoStatus status
    ) {
        List<ServicoEntity> entities = service.findByStatus(status);
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Remove o serviço com o identificador informado.
     *
     * @param id - identificador do serviço a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remove um serviço",
            description = "Remove o serviço com o identificador informado, desde que não tenha agendamento em"
                    + " AGENDADO vinculado."
    )
    @ApiResponse(responseCode = "204", description = "Serviço removido", content = @Content)
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
            description = "Serviço não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_NAO_ENCONTRADO)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Serviço com agendamento em AGENDADO vinculado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.SERVICO_COM_AGENDAMENTO_PENDENTE)
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
            @Parameter(description = "Identificador do serviço", example = "1") @PathVariable Long id
    ) {
        log.debug("delete servico: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
