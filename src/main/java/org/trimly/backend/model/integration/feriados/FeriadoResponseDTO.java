package org.trimly.backend.model.integration.feriados;

/**
 * Feriado nacional retornado pela BrasilAPI.
 *
 * @param date - data do feriado, no formato {@code yyyy-MM-dd}
 * @param name - nome do feriado
 * @param type - tipo do feriado (ex.: {@code national})
 */
public record FeriadoResponseDTO(String date, String name, String type) {}
