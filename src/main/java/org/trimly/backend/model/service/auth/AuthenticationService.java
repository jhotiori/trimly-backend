package org.trimly.backend.model.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Cadastro e login que devolvem um token JWT, delegando a {@link UsuarioService}.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    /**
     * Regras de negócio de usuários.
     * @see {@link UsuarioService}
     */
    private final UsuarioService usuarioService;

    /**
     * Geração e validação dos tokens JWT.
     * @see {@link TokenService}
     */
    private final TokenService tokenService;

    /**
     * Cadastra um novo usuário e devolve um token já autenticado.
     *
     * @param request - dados do usuário a ser criado
     * @throws UsuarioEmailExistenteException - quando o e-mail já está em uso
     * @return AuthResponseDTO - resposta com o token do usuário recém-criado
     */
    public AuthResponseDTO register(UsuarioCreateDTO request) {
        UsuarioEntity usuario = usuarioService.create(request);
        return new AuthResponseDTO(tokenService.generateToken(usuario));
    }

    /**
     * Autentica o usuário pelo e-mail e senha e devolve um token.
     *
     * @param request - credenciais informadas
     * @throws AuthCredenciaisInvalidasException - quando o e-mail não existe ou a senha não confere
     * @return AuthResponseDTO - resposta com o token do usuário autenticado
     */
    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        UsuarioEntity usuario = usuarioService.findByCredenciais(request.email(), request.senha())
                .orElseThrow(AuthCredenciaisInvalidasException::new);

        return new AuthResponseDTO(tokenService.generateToken(usuario));
    }
}
