package org.trimly.backend.service.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.UsuarioEntity;
import org.trimly.backend.model.entity.enums.AgendamentoStatus;
import org.trimly.backend.model.entity.enums.UsuarioCargo;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.UsuarioRepository;
import org.trimly.backend.model.service.usuario.UsuarioValidator;

@ExtendWith(MockitoExtension.class)
class UsuarioValidatorTests {
    @Mock
    private UsuarioRepository repository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private UsuarioValidator validator;

    /**
     * Verifica que um e-mail ainda não utilizado por nenhum usuário não lança exceção na criação.
     */
    @Test
    void usuario_TestValidateEmailUnicoComEmailDisponivelNaoLancaExcecao() {
        when(repository.existsByEmail("novo@trimly.com")).thenReturn(false);

        assertThatCode(() -> validator.validateEmailUnico("novo@trimly.com", null))
                .doesNotThrowAnyException();
    }

    /**
     * Verifica que um e-mail já utilizado por outro usuário lança {@link UsuarioEmailExistenteException} na criação.
     */
    @Test
    void usuario_TestValidateEmailUnicoComEmailEmUsoLancaExcecao() {
        when(repository.existsByEmail("usado@trimly.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validateEmailUnico("usado@trimly.com", null))
                .isInstanceOf(UsuarioEmailExistenteException.class);
    }

    /**
     * Verifica que, em uma atualização, o próprio usuário é desconsiderado ao verificar o e-mail informado.
     */
    @Test
    void usuario_TestValidateEmailUnicoNaAtualizacaoDesconsideraOProprioUsuario() {
        when(repository.existsByEmailAndIdNot("mesmo@trimly.com", 1L)).thenReturn(false);

        assertThatCode(() -> validator.validateEmailUnico("mesmo@trimly.com", 1L))
                .doesNotThrowAnyException();
        verify(repository, never()).existsByEmail(any());
    }

    /**
     * Verifica que, em uma atualização, um e-mail já pertencente a outro usuário lança
     * {@link UsuarioEmailExistenteException}.
     */
    @Test
    void usuario_TestValidateEmailUnicoNaAtualizacaoComEmailDeOutroUsuarioLancaExcecao() {
        when(repository.existsByEmailAndIdNot("outro@trimly.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> validator.validateEmailUnico("outro@trimly.com", 1L))
                .isInstanceOf(UsuarioEmailExistenteException.class);
    }

    /**
     * Verifica que um cargo nulo não altera o usuário nem consulta o repositório.
     */
    @Test
    void usuario_TestValidateCargoUpdateComNovoCargoNuloNaoLancaExcecao() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setCargo(UsuarioCargo.CLIENTE);

        assertThatCode(() -> validator.validateCargoUpdate(usuario, null)).doesNotThrowAnyException();
        verify(repository, never()).countByCargo(any());
    }

    /**
     * Verifica que solicitar o mesmo cargo já atual do usuário lança {@link UsuarioException}.
     */
    @Test
    void usuario_TestValidateCargoUpdateComMesmoCargoLancaExcecao() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setCargo(UsuarioCargo.CLIENTE);

        assertThatThrownBy(() -> validator.validateCargoUpdate(usuario, UsuarioCargo.CLIENTE))
                .isInstanceOf(UsuarioException.class);
    }

    /**
     * Verifica que rebaixar o único usuário com cargo {@code DONO} lança {@link UsuarioException}.
     */
    @Test
    void usuario_TestValidateCargoUpdateRebaixandoUnicoDonoLancaExcecao() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setCargo(UsuarioCargo.DONO);

        when(repository.countByCargo(UsuarioCargo.DONO)).thenReturn(1L);

        assertThatThrownBy(() -> validator.validateCargoUpdate(usuario, UsuarioCargo.CLIENTE))
                .isInstanceOf(UsuarioException.class);
    }

    /**
     * Verifica que rebaixar um dentre vários usuários com cargo {@code DONO} não lança exceção.
     */
    @Test
    void usuario_TestValidateCargoUpdateRebaixandoUmDeVariosDonosNaoLancaExcecao() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setCargo(UsuarioCargo.DONO);

        when(repository.countByCargo(UsuarioCargo.DONO)).thenReturn(2L);

        assertThatCode(() -> validator.validateCargoUpdate(usuario, UsuarioCargo.CLIENTE))
                .doesNotThrowAnyException();
    }

    /**
     * Verifica que um usuário com agendamento pendente em {@code AGENDADO} lança
     * {@link UsuarioComAgendamentoPendenteException}.
     */
    @Test
    void usuario_TestValidateSemAgendamentoPendenteComAgendamentoPendenteLancaExcecao() {
        when(agendamentoRepository.existsByUsuarioIdAndStatus(1L, AgendamentoStatus.AGENDADO))
                .thenReturn(true);

        assertThatThrownBy(() -> validator.validateSemAgendamentoPendente(1L))
                .isInstanceOf(UsuarioComAgendamentoPendenteException.class);
    }

    /**
     * Verifica que um usuário sem nenhum agendamento pendente não lança exceção.
     */
    @Test
    void usuario_TestValidateSemAgendamentoPendenteSemAgendamentoPendenteNaoLancaExcecao() {
        when(agendamentoRepository.existsByUsuarioIdAndStatus(1L, AgendamentoStatus.AGENDADO))
                .thenReturn(false);

        assertThatCode(() -> validator.validateSemAgendamentoPendente(1L)).doesNotThrowAnyException();
    }
}
