package org.trimly.backend.model.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.auth.AuthCredenciaisInvalidasException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

/**
 * Cadastro e login que devolvem um token JWT, delegando a {@link UsuarioService}.
 */
@Slf4j
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
     * Mapper de usuários.
     * @see {@link UsuarioMapper}
     */
    private final UsuarioMapper mapper;

    /**
     * Cadastra um novo usuário e devolve um token já autenticado.
     *
     * @param request - dados do usuário a ser criado
     * @throws UsuarioEmailExistenteException - quando o e-mail já está em uso
     * @return AuthResponseDTO - resposta com o token e o usuário recém-criado
     */
    public AuthResponseDTO register(UsuarioCreateDTO request) {
        UsuarioEntity usuario = usuarioService.create(request);
        log.info("usuario registrado: id={}, email={}", usuario.getId(), usuario.getEmail());
        return toResponse(usuario);
    }

    /**
     * Autentica o usuário pelo e-mail e senha e devolve um token.
     *
     * @param request - credenciais informadas
     * @throws AuthCredenciaisInvalidasException - quando o e-mail não existe ou a senha não confere
     * @return AuthResponseDTO - resposta com o token e o usuário autenticado
     */
    public AuthResponseDTO login(AuthLoginRequestDTO request) {
        UsuarioEntity usuario = usuarioService.findByCredenciais(request.email(), request.senha()).orElseThrow(() -> {
            log.warn("login falhou: email={}", request.email());
            return new AuthCredenciaisInvalidasException();
        });

        log.info("login realizado: id={}, email={}", usuario.getId(), usuario.getEmail());
        return toResponse(usuario);
    }

    /**
     * Monta a resposta com o token emitido e os dados públicos do usuário.
     *
     * @param usuario - usuário autenticado dono do token
     * @return AuthResponseDTO - resposta com o token e o usuário
     */
    private AuthResponseDTO toResponse(UsuarioEntity usuario) {
        return new AuthResponseDTO(tokenService.generateToken(usuario), mapper.toResponse(usuario));
    }
}
