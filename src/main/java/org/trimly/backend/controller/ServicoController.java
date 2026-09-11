package org.trimly.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.servico.ServicoStatus;
import org.trimly.backend.model.service.servico.ServicoService;
import org.trimly.backend.view.dto.servico.ServicoCreateDTO;
import org.trimly.backend.view.dto.servico.ServicoResponseDTO;
import org.trimly.backend.view.dto.servico.ServicoUpdateDTO;
import org.trimly.backend.view.mapper.ServicoMapper;

/**
 * Controller para serviços, com base em {@code /api/servicos}.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.SERVICOS_ENDPOINT)
@RequiredArgsConstructor
public class ServicoController {
    /**
     * Regras de negócio de serviços.
     * @see {@link ServicoService}
     */
    private final ServicoService service;

    /**
     * Mapper de serviços.
     * @see {@link ServicoMapper}
     */
    private final ServicoMapper mapper;

    /**
     * Cria um serviço e retorna o recurso criado com status 201.
     *
     * @param request - dados do serviço a ser criado
     * @return ResponseEntity - resposta com o serviço criado
     */
    @PostMapping
    public ResponseEntity<ServicoResponseDTO> create(@Valid @RequestBody ServicoCreateDTO request) {
        log.debug("create servico: nome={}, valor={}, duracao={}", request.nome(), request.valor(), request.duracao());

        ServicoEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza os campos informados de um serviço existente.
     *
     * @param id - identificador do serviço a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o serviço atualizado
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody ServicoUpdateDTO request) {
        log.debug(
                "update servico: id={}, nome={}, valor={}, duracao={}, status={}",
                id,
                request.nome(),
                request.valor(),
                request.duracao(),
                request.status());

        ServicoEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista todos os serviços cadastrados.
     *
     * @return ResponseEntity - resposta com a lista de serviços
     */
    @GetMapping
    public ResponseEntity<List<ServicoResponseDTO>> findAll() {
        List<ServicoEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um serviço pelo seu identificador.
     *
     * @param id - identificador do serviço
     * @return ResponseEntity - resposta com o serviço encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> findById(@PathVariable Long id) {
        ServicoEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista os serviços com o status informado.
     *
     * @param status - status usado no filtro
     * @return ResponseEntity - resposta com a lista de serviços
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServicoResponseDTO>> findByStatus(@PathVariable ServicoStatus status) {
        List<ServicoEntity> entities = service.findByStatus(status);
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Remove o serviço com o identificador informado.
     *
     * @param id - identificador do serviço a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.debug("delete servico: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
