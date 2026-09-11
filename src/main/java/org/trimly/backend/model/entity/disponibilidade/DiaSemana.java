package org.trimly.backend.model.entity.disponibilidade;

import java.time.DayOfWeek;

/**
 * Dia da semana das janelas de disponibilidade da barbearia, de {@code SEGUNDA} a {@code DOMINGO},
 * com conversão de e para {@link DayOfWeek}.
 */
public enum DiaSemana {
    SEGUNDA,
    TERCA,
    QUARTA,
    QUINTA,
    SEXTA,
    SABADO,
    DOMINGO;

    /**
     * Converte um {@link DayOfWeek} no valor correspondente de {@code DiaSemana}.
     *
     * @param dayOfWeek - dia da semana do calendário
     * @return DiaSemana - o valor correspondente
     */
    public static DiaSemana fromDayOfWeek(DayOfWeek dayOfWeek) {
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
     * correspondente de {@code DiaSemana}.
     *
     * @param diaSemana - nome do dia da semana, de {@code SEGUNDA} a {@code DOMINGO}
     * @throws IllegalArgumentException - quando o nome informado não corresponde a nenhum dia
     * @return DiaSemana - o valor correspondente
     */
    public static DiaSemana fromString(String diaSemana) {
        return switch (diaSemana.toUpperCase()) {
            case "SEGUNDA" -> SEGUNDA;
            case "TERCA" -> TERCA;
            case "QUARTA" -> QUARTA;
            case "QUINTA" -> QUINTA;
            case "SEXTA" -> SEXTA;
            case "SABADO" -> SABADO;
            case "DOMINGO" -> DOMINGO;
            default -> throw new IllegalArgumentException("Dia da semana inválido: " + diaSemana);
        };
    }
}
