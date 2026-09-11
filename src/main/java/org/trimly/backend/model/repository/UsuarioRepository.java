package org.trimly.backend.model.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

/**
 * Repositório de usuários, com busca por nome e e-mail, verificação de e-mail duplicado e contagem
 * por cargo.
 */
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    /**
     * Retorna os usuários cujo nome corresponde ao padrão informado, ignorando maiúsculas e minúsculas.
     *
     * @param nome - padrão de busca aplicado ao nome, com os curingas já incluídos pelo chamador
     * @return List - usuários correspondentes, vazia quando não houver nenhum
     */
    List<UsuarioEntity> findByNomeLikeIgnoreCase(String nome);

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
     * @return boolean - {@code true} quando já há um usuário com esse e-mail
     */
    boolean existsByEmail(String email);

    /**
     * Indica se o e-mail informado já pertence a outro usuário que não o de id informado.
     *
     * @param email - e-mail a ser verificado
     * @param id - identificador do usuário a desconsiderar na verificação
     * @return boolean - {@code true} quando outro usuário já usa esse e-mail
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
