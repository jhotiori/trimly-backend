package org.trimly.backend.model.entity.disponibilidade;

import java.time.DayOfWeek;

/**
 * Dia da semana de uma disponibilidade, com conversão de e para {@link DayOfWeek}.
 */
public enum DisponibilidadeDiaSemana {
    SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO;

    /**
     * Converte um {@link DayOfWeek} no valor correspondente de `DisponibilidadeDiaSemana`.
     *
     * @param dayOfWeek - dia da semana do calendário
     * @return DisponibilidadeDiaSemana - o valor correspondente
     */
    public static DisponibilidadeDiaSemana fromDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> SEGUNDA;
            case TUESDAY -> TERCA;
            case WEDNESDAY -> QUARTA;
            case THURSDAY -> QUINTA;
            case FRIDAY -> SEXTA;
            case SATURDAY -> SABADO;
            case SUNDAY -> DOMINGO;
        };
    }

    /**
     * Converte o nome do dia da semana, sem diferenciar maiúsculas de minúsculas, no valor
     * correspondente de `DisponibilidadeDiaSemana`.
     *
     * @param diaSemana - nome do dia da semana, de `SEGUNDA` a `DOMINGO`
     * @throws IllegalArgumentException - quando o nome informado não corresponde a nenhum dia
     * @return DisponibilidadeDiaSemana - o valor correspondente
     */
    public static DisponibilidadeDiaSemana fromString(String diaSemana) {
        return valueOf(diaSemana.toUpperCase());
    }
}
