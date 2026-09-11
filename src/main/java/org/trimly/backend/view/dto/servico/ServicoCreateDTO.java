package org.trimly.backend.view.dto.servico;

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
        @NotBlank(message = "Nome não pode ser vazio") String nome,

        @NotNull(message = "Valor não pode ser vazio") @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "Duração não pode ser vazia") @Positive(message = "Duração deve ser positiva")
        Integer duracao) {}
