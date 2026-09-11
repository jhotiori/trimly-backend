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
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeResponseDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

/**
 * Controller para disponibilidades, com base em {@code /api/disponibilidades}.
 */
@Slf4j
@RestController
@RequestMapping(EndpointConfig.DISPONIBILIDADES_ENDPOINT)
@RequiredArgsConstructor
public class DisponibilidadeController {
    /**
     * Regras de negócio de disponibilidades.
     * @see {@link DisponibilidadeService}
     */
    private final DisponibilidadeService service;

    /**
     * Mapper de disponibilidades.
     * @see {@link DisponibilidadeMapper}
     */
    private final DisponibilidadeMapper mapper;

    /**
     * Cria uma disponibilidade e retorna o recurso criado com status 201.
     *
     * @param request - dados da disponibilidade a ser criada
     * @return ResponseEntity - resposta com a disponibilidade criada
     */
    @PostMapping
    public ResponseEntity<DisponibilidadeResponseDTO> create(@Valid @RequestBody DisponibilidadeCreateDTO request) {
        log.debug(
                "create disponibilidade: diaSemana={}, horaInicio={}, horaFim={}",
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim());

        DisponibilidadeEntity entity = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(entity));
    }

    /**
     * Atualiza os campos informados de uma disponibilidade existente.
     *
     * @param id - identificador da disponibilidade a ser atualizada
     * @param request - campos a atualizar
     * @return ResponseEntity - resposta com a disponibilidade atualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody DisponibilidadeUpdateDTO request) {
        log.debug(
                "update disponibilidade: id={}, diaSemana={}, horaInicio={}, horaFim={}",
                id,
                request.diaSemana(),
                request.horaInicio(),
                request.horaFim());

        DisponibilidadeEntity entity = service.update(id, request);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista todas as disponibilidades cadastradas.
     *
     * @return ResponseEntity - resposta com a lista de disponibilidades
     */
    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>> findAll() {
        List<DisponibilidadeEntity> entities = service.findAll();
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Busca uma disponibilidade pelo seu identificador.
     *
     * @param id - identificador da disponibilidade
     * @return ResponseEntity - resposta com a disponibilidade encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponseDTO> findById(@PathVariable Long id) {
        DisponibilidadeEntity entity = service.findById(id);
        return ResponseEntity.ok(mapper.toResponse(entity));
    }

    /**
     * Lista as disponibilidades cadastradas para o dia da semana informado.
     *
     * @param diaSemana - dia da semana usado no filtro
     * @return ResponseEntity - resposta com a lista de disponibilidades do dia
     */
    @GetMapping("/dia/{diaSemana}")
    public ResponseEntity<List<DisponibilidadeResponseDTO>> findByDiaSemana(@PathVariable String diaSemana) {
        List<DisponibilidadeEntity> entities = service.findByDiaSemana(DiaSemana.fromString(diaSemana));
        return ResponseEntity.ok(mapper.toResponseList(entities));
    }

    /**
     * Remove a disponibilidade com o identificador informado.
     *
     * @param id - identificador da disponibilidade a ser removida
     * @return ResponseEntity - resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.debug("delete disponibilidade: id={}", id);

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
