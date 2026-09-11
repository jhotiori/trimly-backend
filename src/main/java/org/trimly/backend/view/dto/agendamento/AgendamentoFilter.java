package org.trimly.backend.view.dto.agendamento;

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
public record AgendamentoFilter(AgendamentoStatus status, LocalDate data, Long usuarioId, Long servicoId) {}
