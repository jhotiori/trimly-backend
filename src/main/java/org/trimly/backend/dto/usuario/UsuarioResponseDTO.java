package org.trimly.backend.dto.usuario;

import org.trimly.backend.entity.enums.CargoUsuario;

public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    CargoUsuario cargo
) {}