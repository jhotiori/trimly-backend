package org.trimly.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.trimly.backend.dto.agendamento.CreateAgendamentoDTO;
import org.trimly.backend.entity.AgendamentoEntity;
import org.trimly.backend.entity.ServicoEntity;
import org.trimly.backend.entity.UsuarioEntity;
import org.trimly.backend.entity.enums.StatusAgendamento;
import org.trimly.backend.repository.AgendamentoRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {
    private final AgendamentoRepository repository;
    private final UsuarioService usuarioService;
    private final ServicoService servicoService;

    // ---exists---
    public Boolean existsById(Long id){
        return repository.existsById(id);
    }

    // ---find---
    public AgendamentoEntity findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // ---list---
    public List<AgendamentoEntity> listByUsuarioId(Long id){
        UsuarioEntity usuario = usuarioService.findById(id);
        if(usuario == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }
        return repository.findByUsuario(usuario);
    }

    public List<AgendamentoEntity> listByServicoId(Long id){
        ServicoEntity servico = servicoService.findById(id);
        if(servico == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }

        return repository.findByServico(servico);
    }

    public List<AgendamentoEntity> listByHorario(LocalDateTime horario){
        return repository.findByHorario(horario);
    }

    public List<AgendamentoEntity> findAll(){
        return repository.findAll();
    }

    // ---save---
    public AgendamentoEntity save(CreateAgendamentoDTO dto){
        AgendamentoEntity agendamento = new AgendamentoEntity();
        agendamento.setHorario(LocalDateTime.from(dto.horario())); //conferir tipagem
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setDuracao(dto.duracao());

        UsuarioEntity usuario = usuarioService.findById(dto.usuarioId());
        if(usuario == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }
        agendamento.setUsuario(usuario);

        ServicoEntity servico = servicoService.findById(dto.servicoId());
        if(servico == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }
        agendamento.setServico(servico);

        return repository.save(agendamento);
    }

    // ---update---
    public AgendamentoEntity update(CreateAgendamentoDTO dto, Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        agendamento.setHorario(LocalDateTime.from(dto.horario())); //conferir tipagem
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setDuracao(dto.duracao());

        UsuarioEntity usuario = usuarioService.findById(dto.usuarioId());
        if(usuario == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }
        agendamento.setUsuario(usuario);

        ServicoEntity servico = servicoService.findById(dto.servicoId());
        if(servico == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }
        agendamento.setServico(servico);

        return this.repository.save(agendamento);
    }

    // ---partial update---
    public AgendamentoEntity partialUpdate(CreateAgendamentoDTO dto, Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        if(dto.horario() != null) agendamento.setHorario(LocalDateTime.from(dto.horario()));
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        if(dto.duracao() != null) agendamento.setDuracao(dto.duracao());
        if(dto.usuarioId() != null && usuarioService.existsById(dto.usuarioId())) agendamento.setUsuario(usuarioService.findById(dto.usuarioId()));
        if(dto.servicoId() != null && servicoService.existsById(dto.servicoId())) agendamento.setServico(servicoService.findById(dto.servicoId()));

        return this.repository.save(agendamento);
    }

    public AgendamentoEntity setStatusAgendado(Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        agendamento.setStatus(StatusAgendamento.AGENDADO);
        return this.repository.save(agendamento);
    }

    public AgendamentoEntity setStatusAusente(Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        agendamento.setStatus(StatusAgendamento.AUSENTE);
        return this.repository.save(agendamento);
    }

    public AgendamentoEntity setStatusCancelado(Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return this.repository.save(agendamento);
    }

    public AgendamentoEntity setStatusConcluido(Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Agendamento não encontrado!");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        return this.repository.save(agendamento);
    }

    // ---delete---
    public void deleteAgendamento(Long id){
        AgendamentoEntity agendamento = this.repository.findById(id).orElse(null);
        if(agendamento == null){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }
        //INSERIR VALIDACOES DE DEPENDENCIAS

        this.repository.delete(agendamento);
    }

}
