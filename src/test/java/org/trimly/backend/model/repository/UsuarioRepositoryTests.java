package org.trimly.backend.model.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;

/**
 * Testes para {@link UsuarioRepository}.
 */
@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
class UsuarioRepositoryTests {
    /**
     * Repositório de usuários sob teste.
     * @see {@link UsuarioRepository}
     */
    @Autowired
    private UsuarioRepository repository;

    @Test
    @DisplayName("Encontra usuário pelo e-mail cadastrado")
    void testeDadoEmailCadastradoQuandoBuscarPorEmailEntaoRetornaUsuario() {
        salvar("Ana", "ana@trimly.com", UsuarioCargo.CLIENTE);

        Optional<UsuarioEntity> resultado = repository.findByEmail("ana@trimly.com");

        assertEquals("Ana", resultado.orElseThrow().getNome());
    }

    @Test
    @DisplayName("Retorna vazio para e-mail não cadastrado")
    void testeDadoEmailNaoCadastradoQuandoBuscarPorEmailEntaoRetornaVazio() {
        Optional<UsuarioEntity> resultado = repository.findByEmail("x@trimly.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Confirma e-mail existente e nega e-mail livre")
    void testeDadoEmailsQuandoVerificarExistenciaEntaoDiferenciaCadastradoDeLivre() {
        salvar("Ana", "ana@trimly.com", UsuarioCargo.CLIENTE);

        assertTrue(repository.existsByEmail("ana@trimly.com"));
        assertFalse(repository.existsByEmail("x@trimly.com"));
    }

    @Test
    @DisplayName("Desconsidera o próprio usuário ao checar e-mail")
    void testeDadoEmailDoProprioUsuarioQuandoVerificarExistenciaExcluindoIdEntaoRetornaFalso() {
        UsuarioEntity ana = salvar("Ana", "ana@trimly.com", UsuarioCargo.CLIENTE);

        assertFalse(repository.existsByEmailAndIdNot("ana@trimly.com", ana.getId()));
    }

    @Test
    @DisplayName("Detecta e-mail pertencente a outro usuário")
    void testeDadoEmailDeOutroUsuarioQuandoVerificarExistenciaExcluindoIdEntaoRetornaVerdadeiro() {
        salvar("Ana", "ana@trimly.com", UsuarioCargo.CLIENTE);
        UsuarioEntity beto = salvar("Beto", "beto@trimly.com", UsuarioCargo.CLIENTE);

        assertTrue(repository.existsByEmailAndIdNot("ana@trimly.com", beto.getId()));
    }

    @Test
    @DisplayName("Conta apenas usuários do cargo informado")
    void testeDadoUsuariosDeCargosDiferentesQuandoContarPorCargoEntaoConsideraSoOCargo() {
        salvar("Dona", "dona@trimly.com", UsuarioCargo.DONO);
        salvar("Dono", "dono@trimly.com", UsuarioCargo.DONO);
        salvar("Ana", "ana@trimly.com", UsuarioCargo.CLIENTE);

        assertEquals(2L, repository.countByCargo(UsuarioCargo.DONO));
    }

    private UsuarioEntity salvar(String nome, String email, UsuarioCargo cargo) {
        return repository.save(UsuarioEntity.builder().nome(nome).email(email).senha("hash").cargo(cargo).build());
    }
}
