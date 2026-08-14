package org.trimly.backend.dto.servico;

import org.trimly.backend.entity.enums.StatusServico;
import java.math.BigDecimal;

public record ServicoResponseDTO(
    Long id,
    String nome,
    BigDecimal valor,
    Integer duracao,
    StatusServico status
) {}