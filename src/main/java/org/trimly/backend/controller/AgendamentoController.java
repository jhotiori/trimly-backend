package org.trimly.backend.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.trimly.backend.config.EndpointConfig;
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.service.agendamento.AgendamentoService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoResponseDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Controller para agendamentos, com base em {@code /api/agendamentos}.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.AGENDAMENTOS_ENDPOINT)
@RequiredArgsConstructor
public class AgendamentoController {
    /**
     * Regras de negócio de agendamentos.
     * @see {@link AgendamentoService}
     */
    private final AgendamentoService service;

    /**
     * Mapper de agendamentos.
     * @see {@link AgendamentoMapper}
     */
    private final AgendamentoMapper mapper;

    /**
     * Cria um agendamento e retorna o recurso criado com status 201.
     *
     * @param request - dados do agendamento a ser criado
     * @return ResponseEntity - resposta com o agendamento criado
     */
    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> create(@Valid @RequestBody AgendamentoCreateDTO request) {
        log.debug(
                "create agendamento: data={}, horario={}, usuarioId={}, servicoId={}",
                request.data(),
                request.horario(),
                request.usuarioId(),
                request.servicoId());

        AgendamentoEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza a data e/ou o serviço de um agendamento existente.
     *
     * @param id - identificador do agendamento a ser atualizado
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com o agendamento atualizado
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody AgendamentoUpdateDTO request) {
        log.debug(
                "update agendamento: id={}, data={}, status={}, servicoId={}",
                id,
                request.data(),
                request.status(),
                request.servicoId());

        AgendamentoEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista os agendamentos, aplicando os filtros opcionais informados.
     *
     * @param status - filtro opcional por status
     * @param data - filtro opcional por data
     * @param usuarioId - filtro opcional por usuário
     * @param servicoId - filtro opcional por serviço
     * @return ResponseEntity - resposta com a lista de agendamentos
     */
    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> findAll(
            @RequestParam(required = false) AgendamentoStatus status,
            @RequestParam(required = false) LocalDate data,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long servicoId) {
        AgendamentoFilter filtro = new AgendamentoFilter(status, data, usuarioId, servicoId);
        List<AgendamentoEntity> entities = service.findAll(filtro);
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca um agendamento pelo seu identificador.
     *
     * @param id - identificador do agendamento
     * @return ResponseEntity - resposta com o agendamento encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponseDTO> findById(@PathVariable Long id) {
        AgendamentoEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Remove o agendamento com o identificador informado.
     *
     * @param id - identificador do agendamento a ser removido
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.debug("delete agendamento: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
