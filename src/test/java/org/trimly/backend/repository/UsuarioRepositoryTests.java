package org.trimly.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.UsuarioCargo;
import org.trimly.backend.model.repository.UsuarioRepository;

@RepositoryTest
class UsuarioRepositoryTests {
    @Autowired
    private UsuarioRepository repository;

    /**
     * Verifica que {@code existsByEmailAndIdNot} retorna {@code true} quando o e-mail já pertence a outro
     * usuário.
     */
    @Test
    void usuario_TestExistsByEmailAndIdNotComEmailDeOutroUsuarioRetornaTrue() {
        UsuarioEntity usuario = repository.saveAndFlush(criarUsuario("a@trimly.com", UsuarioCargo.CLIENTE));
        repository.saveAndFlush(criarUsuario("b@trimly.com", UsuarioCargo.DONO));

        assertThat(repository.existsByEmailAndIdNot("b@trimly.com", usuario.getId()))
                .isTrue();
    }

    /**
     * Verifica que {@code existsByEmailAndIdNot} retorna {@code false} quando o e-mail é o próprio e-mail do
     * usuário desconsiderado.
     */
    @Test
    void usuario_TestExistsByEmailAndIdNotComEmailDoProprioUsuarioRetornaFalse() {
        UsuarioEntity usuario = repository.saveAndFlush(criarUsuario("a@trimly.com", UsuarioCargo.CLIENTE));

        assertThat(repository.existsByEmailAndIdNot("a@trimly.com", usuario.getId()))
                .isFalse();
    }

    /**
     * Verifica que {@code countByCargo} reflete a quantidade de usuários cadastrados com o cargo informado.
     */
    @Test
    void usuario_TestCountByCargoReflecteLinhasCadastradas() {
        repository.saveAndFlush(criarUsuario("a@trimly.com", UsuarioCargo.CLIENTE));
        repository.saveAndFlush(criarUsuario("b@trimly.com", UsuarioCargo.DONO));
        repository.saveAndFlush(criarUsuario("c@trimly.com", UsuarioCargo.CLIENTE));

        assertThat(repository.countByCargo(UsuarioCargo.CLIENTE)).isEqualTo(2L);
        assertThat(repository.countByCargo(UsuarioCargo.DONO)).isEqualTo(1L);
        assertThat(repository.countByCargo(UsuarioCargo.ADMIN)).isZero();
    }

    private UsuarioEntity criarUsuario(String email, UsuarioCargo cargo) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNome("Nome");
        entity.setEmail(email);
        entity.setSenha("hash");
        entity.setCargo(cargo);
        return entity;
    }
}
