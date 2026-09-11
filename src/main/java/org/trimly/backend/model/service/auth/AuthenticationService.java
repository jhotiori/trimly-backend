package org.trimly.backend.model.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Orquestra o cadastro e o login, entregando um token JWT em ambos os casos.
 *
 * O cadastro delega a criação do usuário a {@link UsuarioService}, que mantém a
 * unicidade do e-mail, a criptografia da senha e o cargo padrão. O login confere a
 * senha diretamente com o {@link PasswordEncoder}, sem passar pelo
 * {@code AuthenticationManager} do Spring Security.
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
     * Codificador BCrypt usado para conferir a senha no login.
     * @see {@link PasswordEncoder}
     */
    private final PasswordEncoder passwordEncoder;

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
        UsuarioEntity usuario;
        try {
            usuario = usuarioService.findByEmail(request.email());
        } catch (EntityNotFoundException exception) {
            throw new AuthCredenciaisInvalidasException();
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new AuthCredenciaisInvalidasException();
        }

        return new AuthResponseDTO(tokenService.generateToken(usuario));
    }
}
