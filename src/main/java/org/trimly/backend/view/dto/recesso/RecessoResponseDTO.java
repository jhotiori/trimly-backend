package org.trimly.backend.view.dto.recesso;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Representação de resposta de um recesso.
 *
 * @param id - identificador do recesso
 * @param descricao - descrição do recesso
 * @param dataInicio - primeiro dia do recesso
 * @param dataFim - último dia do recesso
 */
public record RecessoResponseDTO(
        @Schema(description = "Identificador do recesso", example = "1") Long id,

        @Schema(description = "Descrição do recesso", example = "Férias da Barbearia") String descricao,

        @Schema(description = "Primeiro dia do recesso", example = "2026-12-01") LocalDate dataInicio,

        @Schema(description = "Último dia do recesso", example = "2026-12-17") LocalDate dataFim
) {}
