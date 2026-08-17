package org.trimly.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.entity.UsuarioEntity;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByNome(String nome);
    boolean existsByEmail(String email); // Verifica se já existe um usuário com aquele e-mail
}
