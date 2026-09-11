package org.trimly.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.UsuarioCargo;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.UsuarioRepository;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.model.service.usuario.UsuarioValidator;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {
    @Mock
    private UsuarioRepository repository;

    @Mock
    private UsuarioMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioValidator usuarioValidator;

    @InjectMocks
    private UsuarioService service;

    /**
     * Verifica que a criação define o cargo como {@code CLIENTE}, criptografa a senha informada e valida a
     * unicidade do e-mail antes de persistir.
     */
    @Test
    void usuario_TestCreateDefineCargoClienteCodificaSenhaEValidaEmailUnico() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Nome", "email@trimly.com", "senha123");
        UsuarioEntity mapeada = new UsuarioEntity();
        mapeada.setNome("Nome");
        mapeada.setEmail("email@trimly.com");

        when(mapper.toEntity(request)).thenReturn(mapeada);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-senha123");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioEntity resultado = service.create(request);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCargo()).isEqualTo(UsuarioCargo.CLIENTE);
        assertThat(captor.getValue().getSenha()).isEqualTo("hash-senha123");
        verify(usuarioValidator).validateEmailUnico("email@trimly.com", null);
        assertThat(resultado.getCargo()).isEqualTo(UsuarioCargo.CLIENTE);
    }

    /**
     * Verifica que uma atualização com todos os campos nulos é um no-op: retorna a entidade inalterada sem
     * escrever no repositório.
     */
    @Test
    void usuario_TestUpdateComDtoTodoNuloNaoChamaRepository() {
        UsuarioEntity existente = new UsuarioEntity();
        existente.setId(1L);
        existente.setCargo(UsuarioCargo.CLIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        UsuarioEntity resultado = service.update(1L, new UsuarioUpdateDTO(null, null, null, null));

        assertThat(resultado).isEqualTo(existente);
        verify(repository, never()).save(any());
        verify(usuarioValidator, never()).validateEmailUnico(any(), any());
    }

    /**
     * Verifica que uma atualização sem o campo de e-mail não revalida a unicidade do e-mail.
     */
    @Test
    void usuario_TestUpdateSemEmailNaoRevalidaEmail() {
        UsuarioEntity existente = new UsuarioEntity();
        existente.setId(1L);
        existente.setNome("Nome Antigo");
        existente.setCargo(UsuarioCargo.CLIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.update(1L, new UsuarioUpdateDTO("Nome Novo", null, null, null));

        verify(usuarioValidator, never()).validateEmailUnico(any(), any());
    }

    /**
     * Verifica que uma atualização com e-mail informado revalida sua unicidade, desconsiderando o próprio
     * usuário.
     */
    @Test
    void usuario_TestUpdateComEmailRevalidaUnicidade() {
        UsuarioEntity existente = new UsuarioEntity();
        existente.setId(1L);
        existente.setEmail("antigo@trimly.com");
        existente.setCargo(UsuarioCargo.CLIENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        service.update(1L, new UsuarioUpdateDTO(null, "novo@trimly.com", null, null));

        verify(usuarioValidator).validateEmailUnico("novo@trimly.com", 1L);
    }

    /**
     * Verifica que uma exceção lançada pelo validator ao atualizar o cargo é propagada sem persistir a
     * entidade.
     */
    @Test
    void usuario_TestUpdatePropagaExcecaoDeCargoUpdate() {
        UsuarioEntity existente = new UsuarioEntity();
        existente.setId(1L);
        existente.setCargo(UsuarioCargo.DONO);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new UsuarioException("Não é possível remover o cargo DONO do único dono cadastrado"))
                .when(usuarioValidator)
                .validateCargoUpdate(existente, UsuarioCargo.CLIENTE);

        assertThatThrownBy(() -> service.update(1L, new UsuarioUpdateDTO(null, null, null, UsuarioCargo.CLIENTE)))
                .isInstanceOf(UsuarioException.class);
        verify(repository, never()).save(any());
    }

    /**
     * Verifica que a remoção propaga {@link UsuarioComAgendamentoPendenteException} sem chegar a remover o
     * usuário do repositório.
     */
    @Test
    void usuario_TestDeleteByIdPropagaExcecaoAntesDeRemover() {
        UsuarioEntity existente = new UsuarioEntity();
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        doThrow(new UsuarioComAgendamentoPendenteException(
                        "Não é possível remover um usuário com agendamento pendente"))
                .when(usuarioValidator)
                .validateSemAgendamentoPendente(1L);

        assertThatThrownBy(() -> service.deleteById(1L)).isInstanceOf(UsuarioComAgendamentoPendenteException.class);
        verify(repository, never()).deleteById(any());
    }
}
