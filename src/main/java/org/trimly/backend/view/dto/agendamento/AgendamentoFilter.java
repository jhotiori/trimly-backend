package org.trimly.backend.view.dto.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;

/**
 * Filtros opcionais da listagem de agendamentos; nulos são ignorados.
 */
public record AgendamentoFilter(
        @Schema(description = "Filtra pelo status do agendamento", example = "AGENDADO") AgendamentoStatus status,

        @Schema(
                description = "Filtra pelo dia do atendimento, no formato yyyy-MM-dd",
                example = "2027-03-15"
        ) LocalDate data,

        @Schema(description = "Filtra pelo usuário atendido", example = "2") Long usuarioId,

        @Schema(description = "Filtra pelo serviço agendado", example = "1") Long servicoId
) {}
