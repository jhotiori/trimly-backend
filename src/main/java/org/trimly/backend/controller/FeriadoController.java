package org.trimly.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.infra.EndpointConfig;
import org.trimly.backend.infra.openapi.OpenApiExamples;
import org.trimly.backend.model.integration.feriados.FeriadoResponseDTO;
import org.trimly.backend.model.service.feriado.FeriadoService;
import org.trimly.backend.view.dto.exception.ErrorResponseDTO;

/**
 * Controller de feriados nacionais, em `/api/feriados`.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.FERIADOS_ENDPOINT)
@RequiredArgsConstructor
@Tag(name = "Feriados", description = "Consulta dos feriados nacionais (BrasilAPI)")
public class FeriadoController {
    /**
     * Menor ano aceito pela BrasilAPI.
     */
    private static final int ANO_MIN = 1900;

    /**
     * Maior ano aceito pela BrasilAPI.
     */
    private static final int ANO_MAX = 2199;

    /**
     * Consulta de feriados nacionais.
     * @see {@link FeriadoService}
     */
    private final FeriadoService service;

    /**
     * Lista os feriados nacionais do ano informado ou do ano atual.
     *
     * @param ano - ano de referência; ausente usa o ano atual
     * @return ResponseEntity - resposta com a lista de feriados do ano
     */
    @GetMapping
    @Operation(summary = "Lista os feriados nacionais de um ano")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de feriados do ano",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = FeriadoResponseDTO.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ano não numérico ou fora de 1900 a 2199",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.PARAMETRO_INVALIDO)
            )
    )
    @ApiResponse(
            responseCode = "503",
            description = "BrasilAPI indisponível",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponseDTO.class),
                    examples = @ExampleObject(value = OpenApiExamples.FERIADOS_INDISPONIVEIS)
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
    public ResponseEntity<List<FeriadoResponseDTO>> findByAno(
            @Parameter(description = "Ano de referência; padrão é o ano atual", example = "2026")
            @RequestParam(required = false)
            @Min(ANO_MIN)
            @Max(ANO_MAX) Integer ano
    ) {
        int anoConsulta = Objects.requireNonNullElse(ano, Year.now().getValue());
        log.debug("find feriados: ano={}", anoConsulta);

        return ResponseEntity.ok(service.findByAno(anoConsulta));
    }
}
