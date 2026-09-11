package org.trimly.backend.view.dto.disponibilidade;

import java.time.LocalTime;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;

/**
 * Campos atualizáveis de uma disponibilidade. Todos são opcionais (semântica de PATCH); uma
 * requisição com todos nulos é um no-op.
 *
 * @param diaSemana - novo dia da semana
 * @param horaInicio - nova hora de início
 * @param horaFim - nova hora de fim
 */
public record DisponibilidadeUpdateDTO(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim) {}
