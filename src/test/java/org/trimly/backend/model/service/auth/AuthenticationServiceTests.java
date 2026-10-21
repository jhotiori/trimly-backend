package org.trimly.backend.model.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Testes para {@link AuthenticationService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {
    /**
     * Serviço de usuários simulado.
     * @see {@link UsuarioService}
     */
    @Mock
    private UsuarioService usuarioService;

    /**
     * Serviço de tokens simulado.
     * @see {@link TokenService}
     */
    @Mock
    private TokenService tokenService;

    /**
     * Serviço sob teste, com os mocks injetados.
     * @see {@link AuthenticationService}
     */
    @InjectMocks
    private AuthenticationService service;

    @Test
    @DisplayName("Registra o usuário e devolve o token gerado")
    void testeDadoUsuarioValidoQuandoRegistrarEntaoRetornaToken() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123456");
        UsuarioEntity usuario = usuario();
        when(usuarioService.create(request)).thenReturn(usuario);
        when(tokenService.generateToken(usuario)).thenReturn("token-jwt");

        AuthResponseDTO resposta = service.register(request);

        assertEquals("token-jwt", resposta.token());
    }

    @Test
    @DisplayName("Não gera token quando o registro falha")
    void testeDadoEmailExistenteQuandoRegistrarEntaoLancaExcecaoSemGerarToken() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Ana", "ana@trimly.com", "123456");
        when(usuarioService.create(request)).thenThrow(new UsuarioEmailExistenteException("E-mail em uso"));

        assertThrows(UsuarioEmailExistenteException.class, () -> service.register(request));

        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("Autentica credenciais corretas e devolve o token gerado")
    void testeDadoCredenciaisCorretasQuandoLogarEntaoRetornaToken() {
        UsuarioEntity usuario = usuario();
        when(usuarioService.findByCredenciais("ana@trimly.com", "123456")).thenReturn(Optional.of(usuario));
        when(tokenService.generateToken(usuario)).thenReturn("token-jwt");

        AuthResponseDTO resposta = service.login(new AuthLoginRequestDTO("ana@trimly.com", "123456"));

        assertEquals("token-jwt", resposta.token());
    }

    @Test
    @DisplayName("Recusa credenciais incorretas sem gerar token")
    void testeDadoCredenciaisIncorretasQuandoLogarEntaoLancaCredenciaisInvalidas() {
        when(usuarioService.findByCredenciais("ana@trimly.com", "errada")).thenReturn(Optional.empty());

        AuthLoginRequestDTO request = new AuthLoginRequestDTO("ana@trimly.com", "errada");

        assertThrows(AuthCredenciaisInvalidasException.class, () -> service.login(request));

        verifyNoInteractions(tokenService);
    }

    private static UsuarioEntity usuario() {
        return UsuarioEntity.builder().id(1L).email("ana@trimly.com").cargo(UsuarioCargo.CLIENTE).build();
    }
}
