package org.trimly.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.model.service.auth.AuthenticationService;
import org.trimly.backend.view.dto.auth.AuthLoginRequestDTO;
import org.trimly.backend.view.dto.auth.AuthResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;

/**
 * Controller para autenticação, com base em {@code /api/auth}, expondo o cadastro e o login que
 * devolvem um token JWT.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.AUTHENTICATION_ENDPOINT)
@RequiredArgsConstructor
public class AuthenticationController {
    /**
     * Orquestração de cadastro e login.
     * @see {@link AuthenticationService}
     */
    private final AuthenticationService service;

    /**
     * Cadastra um novo usuário e retorna o token com status 201.
     *
     * @param request - dados do usuário a ser criado
     * @return ResponseEntity - resposta com o token do usuário recém-criado
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody UsuarioCreateDTO request) {
        log.debug("register usuario: email={}", request.email());

        AuthResponseDTO response = service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Autentica o usuário e retorna o token com status 200.
     *
     * @param request - credenciais informadas
     * @return ResponseEntity - resposta com o token do usuário autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO request) {
        log.debug("login usuario: email={}", request.email());

        AuthResponseDTO response = service.login(request);
        return ResponseEntity.ok(response);
    }
}
