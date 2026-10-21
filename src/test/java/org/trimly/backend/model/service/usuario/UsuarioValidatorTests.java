package org.trimly.backend.model.service.usuario;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.UsuarioRepository;

/**
 * Testes para {@link UsuarioValidator}.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioValidatorTests {
    /**
     * Repositório de usuários simulado.
     * @see {@link UsuarioRepository}
     */
    @Mock
    private UsuarioRepository repository;

    /**
     * Repositório de agendamentos simulado.
     * @see {@link AgendamentoRepository}
     */
    @Mock
    private AgendamentoRepository agendamentoRepository;

    /**
     * Validador sob teste, com os mocks injetados.
     * @see {@link UsuarioValidator}
     */
    @InjectMocks
    private UsuarioValidator validator;

    @Test
    @DisplayName("Aceita e-mail livre na criação")
    void testeDadoEmailLivreQuandoValidarEmailNaCriacaoEntaoNaoLanca() {
        when(repository.existsByEmail("ana@trimly.com")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateEmailUnico("ana@trimly.com", null));
    }

    @Test
    @DisplayName("Rejeita e-mail em uso na criação")
    void testeDadoEmailEmUsoQuandoValidarEmailNaCriacaoEntaoLancaEmailExistente() {
        when(repository.existsByEmail("ana@trimly.com")).thenReturn(true);

        assertThrows(UsuarioEmailExistenteException.class, () -> validator.validateEmailUnico("ana@trimly.com", null));
    }

    @Test
    @DisplayName("Aceita e-mail livre na atualização")
    void testeDadoEmailLivreQuandoValidarEmailNaAtualizacaoEntaoNaoLanca() {
        when(repository.existsByEmailAndIdNot("ana@trimly.com", 1L)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateEmailUnico("ana@trimly.com", 1L));
    }

    @Test
    @DisplayName("Rejeita e-mail de outro usuário na atualização")
    void testeDadoEmailDeOutroUsuarioQuandoValidarEmailNaAtualizacaoEntaoLancaEmailExistente() {
        when(repository.existsByEmailAndIdNot("ana@trimly.com", 1L)).thenReturn(true);

        assertThrows(UsuarioEmailExistenteException.class, () -> validator.validateEmailUnico("ana@trimly.com", 1L));
    }

    @Test
    @DisplayName("Rejeita senha nula ou com menos de 6 caracteres")
    void testeDadoSenhaNulaOuCurtaQuandoValidarSenhaEntaoLancaExcecao() {
        assertAll(
                () -> assertThrows(UsuarioException.class, () -> validator.validateSenha(null)),
                () -> assertThrows(UsuarioException.class, () -> validator.validateSenha("12345"))
        );
    }

    @Test
    @DisplayName("Aceita senha com exatamente 6 caracteres")
    void testeDadoSenhaCom6CaracteresQuandoValidarSenhaEntaoNaoLanca() {
        assertDoesNotThrow(() -> validator.validateSenha("123456"));
    }

    @Test
    @DisplayName("Aceita atualização sem novo cargo")
    void testeDadoCargoNuloQuandoValidarCargoEntaoNaoLanca() {
        assertDoesNotThrow(() -> validator.validateCargoUpdate(usuario(UsuarioCargo.CLIENTE), null));
    }

    @Test
    @DisplayName("Rejeita repetir o cargo atual")
    void testeDadoMesmoCargoQuandoValidarCargoEntaoLancaCargoRepetido() {
        UsuarioEntity admin = usuario(UsuarioCargo.ADMIN);

        UsuarioException excecao = assertThrows(
                UsuarioException.class,
                () -> validator.validateCargoUpdate(admin, UsuarioCargo.ADMIN)
        );

        assertEquals("O usuário já possui esse cargo", excecao.getMessage());
    }

    @Test
    @DisplayName("Rejeita remover o cargo do único DONO")
    void testeDadoUnicoDonoQuandoValidarCargoEntaoLancaUltimoDono() {
        when(repository.countByCargo(UsuarioCargo.DONO)).thenReturn(1L);

        UsuarioEntity dono = usuario(UsuarioCargo.DONO);

        UsuarioException excecao = assertThrows(
                UsuarioException.class,
                () -> validator.validateCargoUpdate(dono, UsuarioCargo.ADMIN)
        );

        assertEquals("Não é possível remover o cargo DONO do único dono cadastrado", excecao.getMessage());
    }

    @Test
    @DisplayName("Aceita remover o cargo de um DONO quando há outro")
    void testeDadoMaisDeUmDonoQuandoValidarCargoEntaoNaoLanca() {
        when(repository.countByCargo(UsuarioCargo.DONO)).thenReturn(2L);

        assertDoesNotThrow(() -> validator.validateCargoUpdate(usuario(UsuarioCargo.DONO), UsuarioCargo.ADMIN));
    }

    @Test
    @DisplayName("Aceita trocar cargo que não é DONO sem contar donos")
    void testeDadoCargoDiferenteDeDonoQuandoValidarCargoEntaoNaoLancaNemContaDonos() {
        assertDoesNotThrow(() -> validator.validateCargoUpdate(usuario(UsuarioCargo.CLIENTE), UsuarioCargo.ADMIN));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Rejeita remover usuário com agendamento pendente")
    void testeDadoAgendamentoPendenteQuandoValidarRemocaoEntaoLancaPendente() {
        when(agendamentoRepository.existsByUsuarioIdAndStatus(1L, AgendamentoStatus.AGENDADO)).thenReturn(true);

        assertThrows(UsuarioComAgendamentoPendenteException.class, () -> validator.validateSemAgendamentoPendente(1L));
    }

    @Test
    @DisplayName("Aceita remover usuário sem agendamento pendente")
    void testeDadoSemAgendamentoPendenteQuandoValidarRemocaoEntaoNaoLanca() {
        when(agendamentoRepository.existsByUsuarioIdAndStatus(1L, AgendamentoStatus.AGENDADO)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validateSemAgendamentoPendente(1L));
    }

    private static UsuarioEntity usuario(UsuarioCargo cargo) {
        return UsuarioEntity.builder().id(1L).cargo(cargo).build();
    }
}
