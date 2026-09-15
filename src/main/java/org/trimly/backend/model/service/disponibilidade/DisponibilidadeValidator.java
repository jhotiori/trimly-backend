package org.trimly.backend.model.service.disponibilidade;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeConflitoException;
import org.trimly.backend.model.exception.disponibilidade.DisponibilidadeHorarioInvalidoException;
import org.trimly.backend.model.repository.DisponibilidadeRepository;

import lombok.RequiredArgsConstructor;

/**
 * Validador de disponibilidades. Aplica, nesta ordem, a hora de início anterior à hora de fim e a
 * ausência de sobreposição com outra disponibilidade no mesmo dia da semana.
 */
@Component
@RequiredArgsConstructor
public class DisponibilidadeValidator {
    /**
     * Repositório de disponibilidades, usado para checar sobreposição de horário.
     * @see {@link DisponibilidadeRepository}
     */
    private final DisponibilidadeRepository repository;

    /**
     * Verifica se a hora de início é anterior à hora de fim.
     *
     * @param horaInicio - hora de início da disponibilidade
     * @param horaFim - hora de fim da disponibilidade
     * @throws DisponibilidadeHorarioInvalidoException - quando a hora de início não é anterior à de fim
     */
    public void validateHorarios(LocalTime horaInicio, LocalTime horaFim) {
        if (!horaInicio.isBefore(horaFim)) {
            throw new DisponibilidadeHorarioInvalidoException(
                    "Horário de inicio deve ser menor do que o horário de fim");
        }
    }

    /**
     * Verifica se a janela informada se sobrepõe a outra disponibilidade no mesmo dia da semana.
     *
     * @param id - identificador da disponibilidade a ignorar na verificação, ou nulo na criação
     * @param diaSemana - dia da semana da nova janela
     * @param horaInicio - hora de início da nova janela
     * @param horaFim - hora de fim da nova janela
     * @throws DisponibilidadeConflitoException - quando a janela se sobrepõe a outra do mesmo dia
     */
    public void validateConflitoDeHorario(Long id, DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim) {
        List<DisponibilidadeEntity> disponibilidadesExistentes = repository.findByDiaSemana(diaSemana);

        for (DisponibilidadeEntity disponibilidadeExistente : disponibilidadesExistentes) {
            // Durante um update, ignora a própria disponibilidade.
            if (id != null && disponibilidadeExistente.getId().equals(id)) {
                continue;
            }

            boolean inicioExistenteAntesDoFimNovo =
                    disponibilidadeExistente.getHoraInicio().isBefore(horaFim);
            boolean fimExistenteDepoisDoInicioNovo =
                    disponibilidadeExistente.getHoraFim().isAfter(horaInicio);
            boolean existeConflito = inicioExistenteAntesDoFimNovo && fimExistenteDepoisDoInicioNovo;

            if (existeConflito) {
                throw new DisponibilidadeConflitoException(
                        "O horário informado já está ocupado por outra disponibilidade nesse dia");
            }
        }
    }
}
