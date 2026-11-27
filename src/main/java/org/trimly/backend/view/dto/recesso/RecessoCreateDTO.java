package org.trimly.backend.view.dto.recesso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Dados de entrada para a criação de um recesso.
 *
 * @param descricao - descrição do recesso
 * @param dataInicio - primeiro dia do recesso
 * @param dataFim - último dia do recesso
 */
public record RecessoCreateDTO(
        @Schema(description = "Descrição do recesso", example = "Férias da Barbearia")
        @NotBlank(message = "Descrição não pode ser vazia")
        @Size(max = 128, message = "Descrição deve ter no máximo 128 caracteres") String descricao,

        @Schema(description = "Primeiro dia (yyyy-MM-dd), hoje ou depois", example = "2026-12-01")
        @NotNull(message = "Data de início não pode ser nula") LocalDate dataInicio,

        @Schema(description = "Último dia (yyyy-MM-dd), não anterior ao início", example = "2026-12-17")
        @NotNull(message = "Data de fim não pode ser nula") LocalDate dataFim
) {}
