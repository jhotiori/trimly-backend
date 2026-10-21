package org.trimly.backend.model.service.usuario;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.UsuarioRepository;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

/**
 * Testes para {@link UsuarioService}.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {
    /**
     * Repositório de usuários simulado.
     * @see {@link UsuarioRepository}
     */
    @Mock
    private UsuarioRepository repository;

    /**
     * Mapper de usuários simulado.
     * @see {@link UsuarioMapper}
     */
    @Mock
    private UsuarioMapper mapper;

    /**
     * Codificador de senha simulado.
     * @see {@link PasswordEncoder}
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Validador de usuários simulado.
     * @see {@link UsuarioValidator}
     */
    @Mock
    private UsuarioValidator usuarioValidator;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link UsuarioService}
     */
    @InjectMocks
    private UsuarioService service;

    @Test
    @DisplayName("Cria usuário como CLIENTE com a senha criptografada")
    void testeDadoDadosValidosQuandoCriarEntaoSalvaClienteComSenhaCriptografada() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123456");
        UsuarioEntity entity = UsuarioEntity.builder().nome("Ana").email("ana@trimly.com").build();
        when(mapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(repository.save(entity)).thenReturn(entity);

        UsuarioEntity resultado = service.create(request);

        assertAll(
                () -> assertEquals(UsuarioCargo.CLIENTE, resultado.getCargo()),
                () -> assertEquals("hash", resultado.getSenha())
        );
        InOrder ordem = inOrder(usuarioValidator);
        ordem.verify(usuarioValidator).validateSenha("123456");
        ordem.verify(usuarioValidator).validateEmailUnico("ana@trimly.com", null);
    }

    @Test
    @DisplayName("Não cria usuário com senha inválida")
    void testeDadoSenhaInvalidaQuandoCriarEntaoLancaExcecaoSemSalvar() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123");
        doThrow(new UsuarioException("Senha curta")).when(usuarioValidator).validateSenha("123");

        assertThrows(UsuarioException.class, () -> service.create(request));

        verifyNoInteractions(mapper, passwordEncoder, repository);
    }

    @Test
    @DisplayName("Não cria usuário com e-mail já cadastrado")
    void testeDadoEmailExistenteQuandoCriarEntaoLancaExcecaoSemSalvar() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123456");
        when(mapper.toEntity(request)).thenReturn(UsuarioEntity.builder().build());
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        doThrow(new UsuarioEmailExistenteException("E-mail em uso")).when(usuarioValidator)
                .validateEmailUnico("ana@trimly.com", null);

        assertThrows(UsuarioEmailExistenteException.class, () -> service.create(request));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Atualiza todos os campos informados")
    void testeDadoTodosOsCamposQuandoAtualizarEntaoAplicaAlteracoes() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Ana Lima", "lima@trimly.com", "novaSenha", UsuarioCargo.ADMIN);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode("novaSenha")).thenReturn("hash-novo");
        when(repository.save(entity)).thenReturn(entity);

        UsuarioEntity resultado = service.update(1L, request);

        assertAll(
                () -> assertEquals("Ana Lima", resultado.getNome()),
                () -> assertEquals("lima@trimly.com", resultado.getEmail()),
                () -> assertEquals("hash-novo", resultado.getSenha()),
                () -> assertEquals(UsuarioCargo.ADMIN, resultado.getCargo())
        );
        verify(usuarioValidator).validateCargoUpdate(entity, UsuarioCargo.ADMIN);
        verify(usuarioValidator).validateEmailUnico("lima@trimly.com", 1L);
        verify(usuarioValidator).validateSenha("novaSenha");
    }

    @Test
    @DisplayName("Ignora nome, e-mail e senha em branco ao atualizar")
    void testeDadoCamposEmBrancoQuandoAtualizarEntaoMantemValoresOriginais() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        UsuarioEntity resultado = service.update(1L, new UsuarioUpdateDTO(" ", " ", " ", null));

        assertAll(
                () -> assertEquals("Ana", resultado.getNome()),
                () -> assertEquals("ana@trimly.com", resultado.getEmail()),
                () -> assertEquals("hash", resultado.getSenha())
        );
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Não escreve nem valida quando todos os campos são nulos")
    void testeDadoTodosOsCamposNulosQuandoAtualizarEntaoRetornaSemAlterar() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        UsuarioEntity resultado = service.update(1L, new UsuarioUpdateDTO(null, null, null, null));

        assertSame(entity, resultado);
        verifyNoInteractions(usuarioValidator);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando o cargo não pode ser alterado")
    void testeDadoCargoInvalidoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        doThrow(new UsuarioException("Já possui esse cargo")).when(usuarioValidator)
                .validateCargoUpdate(entity, UsuarioCargo.CLIENTE);

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(null, null, null, UsuarioCargo.CLIENTE);

        assertThrows(UsuarioException.class, () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não salva quando o novo e-mail já está em uso")
    void testeDadoEmailEmUsoQuandoAtualizarEntaoLancaExcecaoSemSalvar() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "Ana", "ana@trimly.com")));
        doThrow(new UsuarioEmailExistenteException("E-mail em uso")).when(usuarioValidator)
                .validateEmailUnico("beto@trimly.com", 1L);

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(null, "beto@trimly.com", null, null);

        assertThrows(UsuarioEmailExistenteException.class, () -> service.update(1L, request));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Lança exceção ao atualizar usuário inexistente")
    void testeDadoUsuarioInexistenteQuandoAtualizarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Ana Lima", null, null, null);

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, request));
    }

    @Test
    @DisplayName("Remove usuário sem agendamento pendente")
    void testeDadoUsuarioSemAgendamentoPendenteQuandoDeletarEntaoRemove() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteById(1L);

        verify(usuarioValidator).validateSemAgendamentoPendente(1L);
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("Não remove usuário com agendamento pendente")
    void testeDadoUsuarioComAgendamentoPendenteQuandoDeletarEntaoLancaExcecaoSemRemover() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario(1L, "Ana", "ana@trimly.com")));
        doThrow(new UsuarioComAgendamentoPendenteException("Agendamento pendente")).when(usuarioValidator)
                .validateSemAgendamentoPendente(1L);

        assertThrows(UsuarioComAgendamentoPendenteException.class, () -> service.deleteById(1L));

        verify(repository, never()).delete(any(UsuarioEntity.class));
    }

    @Test
    @DisplayName("Lança exceção ao remover usuário inexistente")
    void testeDadoUsuarioInexistenteQuandoDeletarEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteById(99L));

        verifyNoInteractions(usuarioValidator);
    }

    @Test
    @DisplayName("Lista todos os usuários do repositório")
    void testeDadoUsuariosCadastradosQuandoListarEntaoRetornaTodos() {
        List<UsuarioEntity> usuarios = List.of(usuario(1L, "Ana", "ana@trimly.com"));
        when(repository.findAll()).thenReturn(usuarios);

        assertSame(usuarios, service.findAll());
    }

    @Test
    @DisplayName("Busca usuário existente por id")
    void testeDadoUsuarioExistenteQuandoBuscarPorIdEntaoRetornaUsuario() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        assertSame(entity, service.findById(1L));
    }

    @Test
    @DisplayName("Lança exceção ao buscar usuário inexistente por id")
    void testeDadoUsuarioInexistenteQuandoBuscarPorIdEntaoLancaNaoEncontrado() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> service.findById(99L));

        assertEquals("Usuario não foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("Busca usuário existente por e-mail")
    void testeDadoEmailCadastradoQuandoBuscarPorEmailEntaoRetornaUsuario() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findByEmail("ana@trimly.com")).thenReturn(Optional.of(entity));

        assertSame(entity, service.findByEmail("ana@trimly.com"));
    }

    @Test
    @DisplayName("Lança exceção ao buscar e-mail não cadastrado")
    void testeDadoEmailNaoCadastradoQuandoBuscarPorEmailEntaoLancaNaoEncontrado() {
        when(repository.findByEmail("x@trimly.com")).thenReturn(Optional.empty());

        EntityNotFoundException excecao = assertThrows(
                EntityNotFoundException.class,
                () -> service.findByEmail("x@trimly.com")
        );

        assertEquals("Usuario com email não foi encontrado", excecao.getMessage());
    }

    @Test
    @DisplayName("Retorna o usuário quando e-mail e senha conferem")
    void testeDadoCredenciaisCorretasQuandoBuscarPorCredenciaisEntaoRetornaUsuario() {
        UsuarioEntity entity = usuario(1L, "Ana", "ana@trimly.com");
        when(repository.findByEmail("ana@trimly.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);

        Optional<UsuarioEntity> resultado = service.findByCredenciais("ana@trimly.com", "123456");

        assertSame(entity, resultado.orElseThrow());
    }

    @Test
    @DisplayName("Retorna vazio quando a senha não confere")
    void testeDadoSenhaIncorretaQuandoBuscarPorCredenciaisEntaoRetornaVazio() {
        when(repository.findByEmail("ana@trimly.com")).thenReturn(Optional.of(usuario(1L, "Ana", "ana@trimly.com")));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        Optional<UsuarioEntity> resultado = service.findByCredenciais("ana@trimly.com", "errada");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Retorna vazio sem checar a senha quando o e-mail não existe")
    void testeDadoEmailInexistenteQuandoBuscarPorCredenciaisEntaoRetornaVazioSemChecarSenha() {
        when(repository.findByEmail("x@trimly.com")).thenReturn(Optional.empty());

        Optional<UsuarioEntity> resultado = service.findByCredenciais("x@trimly.com", "123456");

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(passwordEncoder);
    }

    private static UsuarioEntity usuario(Long id, String nome, String email) {
        return UsuarioEntity.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("hash")
                .cargo(UsuarioCargo.CLIENTE)
                .build();
    }
}
