package org.trimly.backend.model.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

/**
 * Repositório de usuários.
 */
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    /**
     * Busca o usuário com o e-mail informado.
     *
     * @param email - e-mail a ser buscado
     * @return Optional - o usuário encontrado, ou vazio quando nenhum usuário usa esse e-mail
     */
    Optional<UsuarioEntity> findByEmail(String email);

    /**
     * Indica se já existe um usuário com o e-mail informado.
     *
     * @param email - e-mail a ser verificado
     * @return boolean - `true` quando já há um usuário com esse e-mail
     */
    boolean existsByEmail(String email);

    /**
     * Indica se o e-mail informado já pertence a outro usuário que não o de id informado.
     *
     * @param email - e-mail a ser verificado
     * @param id - identificador do usuário a desconsiderar na verificação
     * @return boolean - `true` quando outro usuário já usa esse e-mail
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Conta quantos usuários possuem o cargo informado.
     *
     * @param cargo - cargo usado no filtro
     * @return long - quantidade de usuários com esse cargo
     */
    long countByCargo(UsuarioCargo cargo);
}
