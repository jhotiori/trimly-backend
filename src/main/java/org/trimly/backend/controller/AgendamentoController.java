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
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.config.openapi.OpenApiExamples;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.service.agendamento.AgendamentoService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoResponseDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Controller para agendamentos, com base em {@code /api/agendamentos}.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.AGENDAMENTOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gestão de agendamentos da barbearia")
public class AgendamentoController {
    /**
     * Regras de negócio de agendamentos.
     * @see {@link AgendamentoService}
     */
    private final AgendamentoService service;

    /**
     * Mapper de agendamentos.
     * @see {@link AgendamentoMapper}
     */
    private final AgendamentoMapper mapper;

    /**
     * Cria um agendamento e retorna o recurso criado com status 201.
     *
     * @param request - dados do agendamento a ser criado
     * @return ResponseEntity - resposta com o agendamento criado
     */
    @PostMapping
    @Operation(
            summary = "Cria um agendamento",
            description = "Cria um agendamento em AGENDADO para o usuário e o serviço informados, com fim calculado pela"
                    + " duração do serviço. Após localizar usuário e serviço, as regras são aplicadas nesta ordem:"
                    + " início no futuro, início e fim no mesmo dia, início em no máximo 14 dias a partir de agora,"
                    + " uma janela de disponibilidade que comporte todo o horário e nenhuma sobreposição com outro"
                    + " agendamento em AGENDADO. Uma data anterior a hoje é barrada pela validação do corpo (400);"
                    + " hoje com horário já passado retorna 422."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Agendamento criado",
            content = @Content(schema = @Schema(implementation = AgendamentoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo ausente, mal formatado ou com campos inválidos; message reúne as mensagens de validação",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "Campos inválidos",
                                    value = OpenApiExamples.AGENDAMENTO_CRIACAO_INVALIDA
                            ),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuário ou serviço não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Usuário", value = OpenApiExamples.USUARIO_NAO_ENCONTRADO),
                            @ExampleObject(name = "Serviço", value = OpenApiExamples.SERVICO_NAO_ENCONTRADO)}
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Horário sobreposto a outro agendamento em AGENDADO",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.AGENDAMENTO_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Início no passado, agendamento que atravessa a meia-noite, início além de 14 dias de"
                    + " antecedência ou sem disponibilidade no horário",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "No passado", value = OpenApiExamples.AGENDAMENTO_NO_PASSADO),
                            @ExampleObject(
                                    name = "Fora do horário",
                                    value = OpenApiExamples.AGENDAMENTO_FORA_DO_HORARIO
                            ),
                            @ExampleObject(
                                    name = "Antecedência excedida",
                                    value = OpenApiExamples.AGENDAMENTO_ANTECEDENCIA_EXCEDIDA
                            ),
                            @ExampleObject(
                                    name = "Dia sem disponibilidade",
                                    value = OpenApiExamples.AGENDAMENTO_SEM_DISPONIBILIDADE
                            ),
                            @ExampleObject(
                                    name = "Horário sem disponibilidade",
                                    value = OpenApiExamples.AGENDAMENTO_DISPONIBILIDADE_INSUFICIENTE
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
    public ResponseEntity<AgendamentoResponseDTO> create(@Valid @RequestBody AgendamentoCreateDTO request) {
        log.debug(
                "create agendamento: data={}, horario={}, usuarioId={}, servicoId={}",
                request.data(),
                request.horario(),
                request.usuarioId(),
                request.servicoId()
        );

        AgendamentoEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza a data e/ou o serviço de um agendamento existente.
     *
     * @param id - identificador do agendamento a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o agendamento atualizado
     */
    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza um agendamento",
            description = "Atualiza data, status e/ou serviço. Campos nulos são ignorados; com todos nulos, devolve o"
                    + " agendamento sem alterações e sem revalidar. Só agendamentos em AGENDADO podem ser alterados, e"
                    + " o novo status não pode repetir o atual. Valem as mesmas regras de horário, antecedência,"
                    + " disponibilidade e conflito da criação, ignorando o próprio agendamento na checagem de"
                    + " conflito."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Agendamento atualizado, ou inalterado quando todos os campos são nulos",
            content = @Content(schema = @Schema(implementation = AgendamentoResponseDTO.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Corpo mal formatado, campos inválidos ou identificador não numérico",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "Campos inválidos",
                                    value = OpenApiExamples.AGENDAMENTO_ATUALIZACAO_INVALIDA
                            ),
                            @ExampleObject(name = "Corpo mal formatado", value = OpenApiExamples.CORPO_INVALIDO),
                            @ExampleObject(name = "Identificador inválido", value = OpenApiExamples.PARAMETRO_INVALIDO)}
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Agendamento ou serviço não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(name = "Agendamento", value = OpenApiExamples.AGENDAMENTO_NAO_ENCONTRADO),
                            @ExampleObject(name = "Serviço", value = OpenApiExamples.SERVICO_NAO_ENCONTRADO)}
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Novo horário sobreposto a outro agendamento em AGENDADO",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.AGENDAMENTO_CONFLITO)
            )
    )
    @ApiResponse(
            responseCode = "422",
            description = "Status que não permite alteração, status repetido ou regra de horário, antecedência e"
                    + " disponibilidade violada",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = {
                            @ExampleObject(
                                    name = "Status não alterável",
                                    value = OpenApiExamples.AGENDAMENTO_STATUS_NAO_ALTERAVEL
                            ),
                            @ExampleObject(
                                    name = "Status repetido",
                                    value = OpenApiExamples.AGENDAMENTO_STATUS_REPETIDO
                            ),
                            @ExampleObject(name = "No passado", value = OpenApiExamples.AGENDAMENTO_NO_PASSADO),
                            @ExampleObject(
                                    name = "Fora do horário",
                                    value = OpenApiExamples.AGENDAMENTO_FORA_DO_HORARIO
                            ),
                            @ExampleObject(
                                    name = "Antecedência excedida",
                                    value = OpenApiExamples.AGENDAMENTO_ANTECEDENCIA_EXCEDIDA
                            ),
                            @ExampleObject(
                                    name = "Dia sem disponibilidade",
                                    value = OpenApiExamples.AGENDAMENTO_SEM_DISPONIBILIDADE
                            ),
                            @ExampleObject(
                                    name = "Horário sem disponibilidade",
                                    value = OpenApiExamples.AGENDAMENTO_DISPONIBILIDADE_INSUFICIENTE
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
    public ResponseEntity<AgendamentoResponseDTO> update(
            @Parameter(description = "Identificador do agendamento", example = "1") @PathVariable Long id,
            @Valid @RequestBody AgendamentoUpdateDTO request
    ) {
        log.debug(
                "update agendamento: id={}, data={}, status={}, servicoId={}",
                id,
                request.data(),
                request.status(),
                request.servicoId()
        );

        AgendamentoEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista os agendamentos, aplicando os filtros opcionais informados.
     *
     * @param status - filtro opcional por status
     * @param data - filtro opcional por data
     * @param usuarioId - filtro opcional por usuário
     * @param servicoId - filtro opcional por serviço
     * @return ResponseEntity - resposta com a lista de agendamentos
     */
    @GetMapping
    @Operation(
            summary = "Lista agendamentos",
            description = "Lista os agendamentos, combinando os filtros opcionais informados. Sem filtros, devolve"
                    + " todos; sem correspondência, devolve uma lista vazia."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de agendamentos, possivelmente vazia",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgendamentoResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Filtro com valor inválido, como status fora do enum ou data fora do formato yyyy-MM-dd",
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
    public ResponseEntity<List<AgendamentoResponseDTO>> findAll(
            @Parameter(description = "Filtra pelo status do agendamento", example = "AGENDADO") @RequestParam(
                    required = false
            ) AgendamentoStatus status,
            @Parameter(
                    description = "Filtra pelo dia do atendimento, no formato yyyy-MM-dd",
                    example = "2027-03-15"
            ) @RequestParam(required = false) LocalDate data,
            @Parameter(description = "Filtra pelo usuário atendido", example = "2") @RequestParam(
                    required = false
            ) Long usuarioId,
            @Parameter(description = "Filtra pelo serviço agendado", example = "1") @RequestParam(
                    required = false
            ) Long servicoId
    ) {
        AgendamentoFilter filtro = new AgendamentoFilter(status, data, usuarioId, servicoId);
        List<AgendamentoEntity> entities = service.findAll(filtro);
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um agendamento pelo seu identificador.
     *
     * @param id - identificador do agendamento
     * @return ResponseEntity - resposta com o agendamento encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Busca um agendamento", description = "Busca um agendamento pelo identificador.")
    @ApiResponse(
            responseCode = "200",
            description = "Agendamento encontrado",
            content = @Content(schema = @Schema(implementation = AgendamentoResponseDTO.class))
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
            description = "Agendamento não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.AGENDAMENTO_NAO_ENCONTRADO)
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
    public ResponseEntity<AgendamentoResponseDTO> findById(
            @Parameter(description = "Identificador do agendamento", example = "1") @PathVariable Long id
    ) {
        AgendamentoEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o agendamento com o identificador informado.
     *
     * @param id - identificador do agendamento a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um agendamento", description = "Remove o agendamento com o identificador informado.")
    @ApiResponse(responseCode = "204", description = "Agendamento removido", content = @Content)
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
            description = "Agendamento não encontrado",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.AGENDAMENTO_NAO_ENCONTRADO)
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
            @Parameter(description = "Identificador do agendamento", example = "1") @PathVariable Long id
    ) {
        log.debug("delete agendamento: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
