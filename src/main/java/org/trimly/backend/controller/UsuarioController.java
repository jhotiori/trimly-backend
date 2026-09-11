package org.trimly.backend.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.service.usuario.UsuarioService;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioLoginRequestDTO;
import org.trimly.backend.view.dto.usuario.UsuarioLoginResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioResponseDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para usuários, com base em {@code /api/usuarios}. Expõe o cadastro público e um login
 * por credenciais que não emite token.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.USUARIOS_ENDPOINT)
@RequiredArgsConstructor
public class UsuarioController {
    /**
     * Regras de negócio de usuários.
     * @see {@link UsuarioService}
     */
    private final UsuarioService service;

    /**
     * Mapper de usuários.
     * @see {@link UsuarioMapper}
     */
    private final UsuarioMapper mapper;

    /**
     * Cria um novo usuário com cargo cliente.
     *
     * @param request - dados do usuário a ser criado
     * @return ResponseEntity - resposta com o usuário criado
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioCreateDTO request) {
        log.debug("create usuario: nome={}, email={}", request.nome(), request.email());

        UsuarioEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Autentica um usuário pelas credenciais informadas. Responde sempre {@code 200}: o campo
     * {@code sucesso} indica o resultado e {@code usuario} é nulo quando as credenciais não conferem.
     *
     * @param request - credenciais informadas no login
     * @return ResponseEntity - resposta com o resultado da autenticação
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginResponseDTO> login(@Valid @RequestBody UsuarioLoginRequestDTO request) {
        log.debug("login usuario: email={}", request.email());

        Optional<UsuarioEntity> usuario = service.findByCredenciais(request.email(), request.senha());
        if (usuario.isEmpty()) {
            return ResponseEntity.ok(new UsuarioLoginResponseDTO(false, null));
        }

        return ResponseEntity.ok(new UsuarioLoginResponseDTO(true, mapper.toResponse(usuario.get())));
    }

    /**
     * Atualiza os campos informados de um usuário existente.
     *
     * @param id - identificador do usuário a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o usuário atualizado
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO request) {
        log.debug(
                "update usuario: id={}, nome={}, email={}, cargo={}",
                id,
                request.nome(),
                request.email(),
                request.cargo());

        UsuarioEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista todos os usuários cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de usuários
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        List<UsuarioEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um usuário pelo seu identificador.
     *
     * @param id - identificador do usuário
     * @return ResponseEntity - resposta com o usuário encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Long id) {
        UsuarioEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o usuário com o identificador informado.
     *
     * @param id - identificador do usuário a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.debug("delete usuario: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
