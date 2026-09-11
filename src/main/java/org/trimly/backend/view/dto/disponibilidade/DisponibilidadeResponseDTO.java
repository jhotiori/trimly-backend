package org.trimly.backend.view.dto.disponibilidade;

import java.time.LocalTime;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;

/**
 * Representação de resposta de uma disponibilidade.
 *
 * @param id - identificador da disponibilidade
 * @param diaSemana - dia da semana da janela de atendimento
 * @param horaInicio - hora de início da janela
 * @param horaFim - hora de fim da janela
 */
public record DisponibilidadeResponseDTO(Long id, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim) {}
