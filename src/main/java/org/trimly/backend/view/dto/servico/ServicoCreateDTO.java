package org.trimly.backend.view.dto.servico;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Dados de entrada para a criação de um serviço.
 *
 * @param nome - nome do serviço
 * @param valor - valor cobrado pelo serviço
 * @param duracao - duração do serviço em minutos
 */
public record ServicoCreateDTO(
        @Schema(description = "Nome do serviço, único no catálogo", example = "Corte Infantil")
        @NotBlank(message = "Nome não pode ser vazio")
        String nome,

        @Schema(description = "Preço cobrado, em reais", example = "35.00")
        @NotNull(message = "Valor não pode ser vazio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @Schema(description = "Duração do atendimento em minutos", example = "30")
        @NotNull(message = "Duração não pode ser vazia")
        @Positive(message = "Duração deve ser positiva")
        Integer duracao) {}
