package org.trimly.backend.view.dto.recesso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Campos atualizáveis de um recesso (PATCH); todos nulos é no-op.
 */
public record RecessoUpdateDTO(
        @Schema(description = "Opcional. Nova descrição", example = "Reforma da Barbearia")
        @Size(max = 128, message = "Descrição deve ter no máximo 128 caracteres") String descricao,

        @Schema(
                description = "Opcional. Novo primeiro dia (yyyy-MM-dd), hoje ou depois",
                example = "2026-12-02"
        ) LocalDate dataInicio,

        @Schema(description = "Opcional. Novo último dia (yyyy-MM-dd)", example = "2026-12-20") LocalDate dataFim
) {}
