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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.trimly.backend.model.entity.agendamento.AgendamentoEntity;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.servico.ServicoEntity;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.service.agendamento.AgendamentoService;
import org.trimly.backend.view.dto.agendamento.AgendamentoCreateDTO;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.mapper.AgendamentoMapper;

/**
 * Testes para {@link AgendamentoController}.
 */
@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AgendamentoMapper.class)
class AgendamentoControllerTests {
    /**
     * Caminho base do controller.
     */
    private static final String ENDPOINT = "/api/agendamentos";

    /**
     * Cliente HTTP simulado para exercitar o controller.
     * @see {@link MockMvc}
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serviço de agendamentos simulado.
     * @see {@link AgendamentoService}
     */
    @MockitoBean
    private AgendamentoService service;

    @Test
    @DisplayName("Cria agendamento válido e retorna 201")
    void testeDadoAgendamentoValidoQuandoCriarEntaoRetornaCriado() throws Exception {
        AgendamentoCreateDTO request = new AgendamentoCreateDTO(LocalDate.of(2999, 1, 10), LocalTime.of(9, 0), 2L, 1L);
        when(service.create(request)).thenReturn(agendamento(1L, AgendamentoStatus.AGENDADO));

        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"2999-01-10\",\"horario\":\"09:00\",\"usuarioId\":2,\"servicoId\":1}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.usuarioId").value(2))
                .andExpect(jsonPath("$.servicoId").value(1));
    }

    @Test
    @DisplayName("Rejeita criação com data no passado e retorna 400")
    void testeDadoDataNoPassadoQuandoCriarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(
                post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"2000-01-10\",\"horario\":\"09:00\",\"usuarioId\":2,\"servicoId\":1}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data deve estar no presente ou futuro"));
    }

    @Test
    @DisplayName("Atualiza agendamento existente e retorna 200")
    void testeDadoAgendamentoExistenteQuandoAtualizarEntaoRetornaOk() throws Exception {
        AgendamentoUpdateDTO request = new AgendamentoUpdateDTO(null, AgendamentoStatus.CANCELADO, null);
        when(service.update(1L, request)).thenReturn(agendamento(1L, AgendamentoStatus.CANCELADO));

        mockMvc.perform(
                patch(ENDPOINT + "/1").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADO\"}")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("Atualiza agendamento inexistente e retorna 404")
    void testeDadoAgendamentoInexistenteQuandoAtualizarEntaoRetornaNaoEncontrado() throws Exception {
        when(service.update(99L, new AgendamentoUpdateDTO(null, AgendamentoStatus.CANCELADO, null)))
                .thenThrow(new EntityNotFoundException("Agendamento não foi encontrado"));

        mockMvc.perform(
                patch(ENDPOINT + "/99").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADO\"}")
        ).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Remove agendamento existente e retorna 204")
    void testeDadoAgendamentoExistenteQuandoDeletarEntaoRetornaSemConteudo() throws Exception {
        mockMvc.perform(delete(ENDPOINT + "/1")).andExpect(status().isNoContent());

        verify(service).deleteById(1L);
    }

    @Test
    @DisplayName("Remove agendamento inexistente e retorna 404")
    void testeDadoAgendamentoInexistenteQuandoDeletarEntaoRetornaNaoEncontrado() throws Exception {
        doThrow(new EntityNotFoundException("Agendamento não foi encontrado")).when(service).deleteById(99L);

        mockMvc.perform(delete(ENDPOINT + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista agendamentos aplicando os filtros informados")
    void testeDadoFiltrosQuandoListarEntaoRepassaFiltroAoService() throws Exception {
        AgendamentoFilter filtro = new AgendamentoFilter(AgendamentoStatus.AGENDADO, LocalDate.of(2999, 1, 10), 2L, 1L);
        when(service.findAll(filtro)).thenReturn(List.of(agendamento(1L, AgendamentoStatus.AGENDADO)));

        mockMvc.perform(
                get(ENDPOINT).param("status", "AGENDADO")
                        .param("data", "2999-01-10")
                        .param("usuarioId", "2")
                        .param("servicoId", "1")
        ).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Lista agendamentos sem filtros")
    void testeDadoSemFiltrosQuandoListarEntaoRepassaFiltroVazioAoService() throws Exception {
        AgendamentoFilter semFiltros = new AgendamentoFilter(null, null, null, null);
        when(service.findAll(semFiltros))
                .thenReturn(List.of(agendamento(1L, AgendamentoStatus.AGENDADO), agendamento(2L, AgendamentoStatus.AUSENTE)));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].status").value("AUSENTE"));
    }

    @Test
    @DisplayName("Rejeita status inexistente na listagem e retorna 400")
    void testeDadoStatusInvalidoQuandoListarEntaoRetornaRequisicaoInvalida() throws Exception {
        mockMvc.perform(get(ENDPOINT).param("status", "INEXISTENTE")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Busca agendamento por id e retorna 200")
    void testeDadoAgendamentoExistenteQuandoBuscarPorIdEntaoRetornaOk() throws Exception {
        when(service.findById(1L)).thenReturn(agendamento(1L, AgendamentoStatus.AGENDADO));

        mockMvc.perform(get(ENDPOINT + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracao").value(30))
                .andExpect(jsonPath("$.data").value("2999-01-10T09:00:00"));
    }

    @Test
    @DisplayName("Busca agendamento inexistente por id e retorna 404")
    void testeDadoAgendamentoInexistenteQuandoBuscarPorIdEntaoRetornaNaoEncontrado() throws Exception {
        when(service.findById(99L)).thenThrow(new EntityNotFoundException("Agendamento não foi encontrado"));

        mockMvc.perform(get(ENDPOINT + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Agendamento não foi encontrado"));
    }

    private static AgendamentoEntity agendamento(Long id, AgendamentoStatus status) {
        return AgendamentoEntity.builder()
                .id(id)
                .data(LocalDateTime.of(2999, 1, 10, 9, 0))
                .duracao(30)
                .status(status)
                .usuario(UsuarioEntity.builder().id(2L).build())
                .servico(ServicoEntity.builder().id(1L).build())
                .build();
    }
}
