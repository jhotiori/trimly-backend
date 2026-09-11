package org.trimly.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.DisponibilidadeEntity;
import org.trimly.backend.model.entity.enums.DiaSemana;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeResponseDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DisponibilidadeController.class)
@AutoConfigureMockMvc(addFilters = false)
class DisponibilidadeControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DisponibilidadeService service;

    @MockitoBean
    private DisponibilidadeMapper mapper;

    /**
     * Verifica que criar uma disponibilidade com um corpo válido retorna 201.
     */
    @Test
    void disponibilidade_TestCreateComCorpoValidoRetorna201() throws Exception {
        DisponibilidadeCreateDTO request =
                new DisponibilidadeCreateDTO(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0));
        DisponibilidadeEntity entity = new DisponibilidadeEntity();
        DisponibilidadeResponseDTO response =
                new DisponibilidadeResponseDTO(1L, DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0));

        when(service.create(any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(post("/api/disponibilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar uma disponibilidade sem o dia da semana:
     * {@code GlobalExceptionHandler} intercepta a {@code MethodArgumentNotValidException} da validação de bean
     * antes que o resolvedor padrão do Spring possa mapeá-la para 400, retornando 500.
     */
    @Test
    void disponibilidade_TestCreateSemDiaSemanaRetorna500() throws Exception {
        String corpo = """
                {"horaInicio":"09:00:00","horaFim":"18:00:00"}""";

        mockMvc.perform(post("/api/disponibilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para criar uma disponibilidade com os horários nulos: a
     * mesma interceptação da {@code MethodArgumentNotValidException} pelo {@code GlobalExceptionHandler} retorna
     * 500 em vez de 400.
     */
    @Test
    void disponibilidade_TestCreateComHorariosNulosRetorna500() throws Exception {
        String corpo = """
                {"diaSemana":"SEGUNDA","horaInicio":null,"horaFim":null}""";

        mockMvc.perform(post("/api/disponibilidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que atualizar uma disponibilidade existente com um corpo válido retorna 200.
     */
    @Test
    void disponibilidade_TestUpdateComCorpoValidoRetorna200() throws Exception {
        DisponibilidadeEntity entity = new DisponibilidadeEntity();
        DisponibilidadeResponseDTO response =
                new DisponibilidadeResponseDTO(1L, DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(19, 0));

        when(service.update(anyLong(), any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(patch("/api/disponibilidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"horaFim":"19:00:00"}"""))
                .andExpect(status().isOk());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para atualizar uma disponibilidade com um horário em
     * formato inválido: {@code GlobalExceptionHandler} intercepta a {@code HttpMessageNotReadableException} da
     * desserialização do JSON antes que o resolvedor padrão do Spring possa mapeá-la para 400, retornando 500.
     */
    @Test
    void disponibilidade_TestUpdateComFormatoDeHorarioInvalidoRetorna500() throws Exception {
        mockMvc.perform(patch("/api/disponibilidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"horaInicio":"nao-e-um-horario"}"""))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que listar as disponibilidades cadastradas retorna 200.
     */
    @Test
    void disponibilidade_TestFindAllRetorna200() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/disponibilidades")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar uma disponibilidade existente pelo id retorna 200.
     */
    @Test
    void disponibilidade_TestFindByIdComIdExistenteRetorna200() throws Exception {
        DisponibilidadeEntity entity = new DisponibilidadeEntity();
        DisponibilidadeResponseDTO response =
                new DisponibilidadeResponseDTO(1L, DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(18, 0));

        when(service.findById(1L)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/disponibilidades/1")).andExpect(status().isOk());
    }

    /**
     * Verifica que buscar uma disponibilidade inexistente pelo id retorna 404.
     */
    @Test
    void disponibilidade_TestFindByIdComIdInexistenteRetorna404() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Disponibilidade não foi encontrada"));

        mockMvc.perform(get("/api/disponibilidades/99")).andExpect(status().isNotFound());
    }

    /**
     * Verifica que listar as disponibilidades de um dia da semana válido retorna 200.
     */
    @Test
    void disponibilidade_TestFindByDiaSemanaComDiaValidoRetorna200() throws Exception {
        when(service.findByDiaSemana(DiaSemana.SEGUNDA)).thenReturn(List.of());
        when(mapper.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/disponibilidades/dia/SEGUNDA")).andExpect(status().isOk());
    }

    /**
     * Verifica o comportamento atual (não o desejado) para um dia da semana inválido: a
     * {@link IllegalArgumentException} lançada por {@code DiaSemana.fromString} escapa sem tratamento até o
     * {@code GlobalExceptionHandler}, retornando 500 em vez de 400.
     */
    @Test
    void disponibilidade_TestFindByDiaSemanaComDiaInvalidoRetorna500() throws Exception {
        mockMvc.perform(get("/api/disponibilidades/dia/NAO_EXISTE")).andExpect(status().isInternalServerError());
    }

    /**
     * Verifica que remover uma disponibilidade existente retorna 204.
     */
    @Test
    void disponibilidade_TestDeleteByIdComIdExistenteRetorna204() throws Exception {
        mockMvc.perform(delete("/api/disponibilidades/1")).andExpect(status().isNoContent());
    }

    /**
     * Verifica que remover uma disponibilidade inexistente retorna 404.
     */
    @Test
    void disponibilidade_TestDeleteByIdComIdInexistenteRetorna404() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("Disponibilidade não foi encontrada"))
                .when(service)
                .deleteById(99L);

        mockMvc.perform(delete("/api/disponibilidades/99")).andExpect(status().isNotFound());
    }
}
