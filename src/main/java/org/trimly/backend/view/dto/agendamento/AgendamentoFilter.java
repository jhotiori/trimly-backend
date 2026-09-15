package org.trimly.backend.view.dto.agendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;

/**
 * Filtros opcionais para a listagem de agendamentos. Campos nulos são ignorados.
 *
 * @param status - filtro por status
 * @param data - filtro por dia
 * @param usuarioId - filtro por usuário
 * @param servicoId - filtro por serviço
 */
public record AgendamentoFilter(
        @Schema(description = "Filtra pelo status do agendamento", example = "AGENDADO")
        AgendamentoStatus status,

        @Schema(description = "Filtra pelo dia do atendimento, no formato yyyy-MM-dd", example = "2027-03-15")
        LocalDate data,

        @Schema(description = "Filtra pelo usuário atendido", example = "2")
        Long usuarioId,

        @Schema(description = "Filtra pelo serviço agendado", example = "1")
        Long servicoId) {}
