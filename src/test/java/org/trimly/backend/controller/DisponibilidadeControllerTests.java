package org.trimly.backend.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.trimly.backend.model.entity.disponibilidade.DiaSemana;
import org.trimly.backend.model.entity.disponibilidade.DisponibilidadeEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.service.disponibilidade.DisponibilidadeService;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeCreateDTO;
import org.trimly.backend.view.dto.disponibilidade.DisponibilidadeUpdateDTO;
import org.trimly.backend.view.mapper.DisponibilidadeMapper;

/**
 * Testes para {@link DisponibilidadeController}.
 */
@WebMvcTest(DisponibilidadeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DisponibilidadeMapper.class)
class DisponibilidadeControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/disponibilidades";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de disponibilidades simulado.
     * @see {@link DisponibilidadeService}
     */
    @MockitoBean
    private DisponibilidadeService service;

    @Test
    @DisplayName("Cria disponibilidade válida e retorna 201")
    void testeDadoDisponibilidadeValidaQuandoCriarEntaoRetornaCriado() throws Exception {
        DisponibilidadeCreateDTO request = new DisponibilidadeCreateDTO(
                DiaSemana.SEXTA,
                LocalTime.of(16, 0),
                LocalTime.of(18, 0)
        );
        when(service.create(request)).thenReturn(disponibilidade(1L, DiaSemana.SEXTA));

        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diaSemana\":\"SEXTA\",\"horaInicio\":\"16:00\",\"horaFim\":\"18:00\"}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.diaSemana").value("SEXTA"))
                .andExpect(jsonPath("$.horaInicio").value("16:00:00"))
                .andExpect(jsonPath("$.horaFim").value("18:00:00"));
    }

    @Test
    @DisplayName("Rejeita criação sem dia da semana e retorna 400")
    void testeDadoDiaSemanaNuloQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"horaInicio\":\"16:00\",\"horaFim\":\"18:00\"}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dia da semana não pode ser nulo"));
    }

    @Test
    @DisplayName("Atualiza disponibilidade existente e retorna 200")
    void testeDadoDisponibilidadeExistenteQuandoAtualizarEntaoRetornaOk() throws Exception {
        DisponibilidadeUpdateDTO request = new DisponibilidadeUpdateDTO(DiaSemana.SABADO, null, null);
        when(service.update(1L, request)).thenReturn(disponibilidade(1L, DiaSemana.SABADO));

        mockMvc.perform(
                patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"diaSemana\":\"SABADO\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.diaSemana").value("SABADO"));
    }

    @Test
    @DisplayName("Atualiza disponibilidade inexistente e retorna 404")
    void testeDadoDisponibilidadeInexistenteQuandoAtualizarEntaoRetornaNaoEncontrado() throws Exception {
        when(service.update(99L, new DisponibilidadeUpdateDTO(DiaSemana.SABADO, null, null)))
                .thenThrow(new EntityNotFoundException("Disponibilidade não foi encontrada"));

        mockMvc.perform(
                patch(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("{\"diaSemana\":\"SABADO\"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Remove disponibilidade existente e retorna 204")
    void testeDadoDisponibilidadeExistenteQuandoDeletarEntaoRetornaSemConteudo() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("Remove disponibilidade inexistente e retorna 404")
    void testeDadoDisponibilidadeInexistenteQuandoDeletarEntaoRetornaNaoEncontrado() throws Exception {
        doThrow(new EntityNotFoundException("Disponibilidade não foi encontrada")).when(service).deleteById(99L);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista todas as disponibilidades e retorna 200")
    void testeDadoDisponibilidadesCadastradasQuandoListarEntaoRetornaLista() throws Exception {
        when(service.findAll())
                .thenReturn(List.of(disponibilidade(1L, DiaSemana.SEGUNDA), disponibilidade(2L, DiaSemana.TERCA)));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].diaSemana").value("TERCA"));
    }

    @Test
    @DisplayName("Busca disponibilidade por id e retorna 200")
    void testeDadoDisponibilidadeExistenteQuandoBuscarPorIdEntaoRetornaOk() throws Exception {
        when(service.findById(1L)).thenReturn(disponibilidade(1L, DiaSemana.SEGUNDA));

        mockMvc.perform(get(ENDPOINT + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaSemana").value("SEGUNDA"));
    }

    @Test
    @DisplayName("Busca disponibilidade inexistente por id e retorna 404")
    void testeDadoDisponibilidadeInexistenteQuandoBuscarPorIdEntaoRetornaNaoEncontrado() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Disponibilidade não foi encontrada"));

        mockMvc.perform(get(ENDPOINT + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Disponibilidade não foi encontrada"));
    }

    @Test
    @DisplayName("Lista disponibilidades por dia, aceitando minúsculas, e retorna 200")
    void testeDadoDiaSemanaEmMinusculasQuandoBuscarPorDiaEntaoRetornaLista() throws Exception {
        when(service.findByDiaSemana(DiaSemana.SEGUNDA)).thenReturn(List.of(disponibilidade(1L, DiaSemana.SEGUNDA)));

        mockMvc.perform(get(ENDPOINT + "/dia/segunda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diaSemana").value("SEGUNDA"));
    }

    private static DisponibilidadeEntity disponibilidade(Long id, DiaSemana diaSemana) {
        return DisponibilidadeEntity.builder()
                .id(id)
                .diaSemana(diaSemana)
                .horaInicio(LocalTime.of(16, 0))
                .horaFim(LocalTime.of(18, 0))
                .build();
    }
}
