package org.trimly.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.trimly.backend.dto.servico.CreateServicoDTO;
import org.trimly.backend.entity.ServicoEntity;
import org.trimly.backend.entity.enums.StatusServico;
import org.trimly.backend.repository.ServicoRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {
    private final ServicoRepository repository;

    // ---exists---
    public Boolean existsById(Long id) {
        return repository.existsById(id);
    }

    // ---find---
    public ServicoEntity findById(Long id) {
        return this.repository.findById(id).orElse(null);
    }

    // ---list---
    public List<ServicoEntity> findByName(String name) {
        return this.repository.findByNomeContainingIgnoreCase(name);
    }

    public List<ServicoEntity> findStatus(StatusServico status) {
        return this.repository.findByStatus(status);
    }

    public List<ServicoEntity> findAll() {
        return this.repository.findAll();
    }

    // ---save---
    public ServicoEntity save(CreateServicoDTO dto) {
        ServicoEntity servico = new ServicoEntity();
        servico.setNome(dto.nome());
        servico.setValor(dto.valor());
        servico.setDuracao(dto.duracao());
        servico.setStatus(StatusServico.INATIVO);

        return this.repository.save(servico);
    }

    // ---update---
    public ServicoEntity update(CreateServicoDTO dto, Long id) {
        ServicoEntity servico = this.repository.findById(id).orElse(null);
        if(servico == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }

        servico.setNome(dto.nome());
        servico.setValor(dto.valor());
        servico.setDuracao(dto.duracao());

        return this.repository.save(servico);
    }

    // ---partial update---
    public ServicoEntity partialUpdate(CreateServicoDTO dto, Long id) {
        ServicoEntity servico = this.repository.findById(id).orElse(null);
        if(servico == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }

        if(dto.nome() != null) servico.setNome(dto.nome());
        if(dto.valor() != null) servico.setValor(dto.valor());
        if(dto.duracao() != null) servico.setDuracao(dto.duracao());

        return this.repository.save(servico);
    }

    public ServicoEntity enableServico(Long id){
        ServicoEntity servico = this.repository.findById(id).orElse(null);
        if(servico == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }

        servico.setStatus(StatusServico.ATIVO);

        return this.repository.save(servico);
    }

    public ServicoEntity disableServico(Long id){
        ServicoEntity servico = this.repository.findById(id).orElse(null);
        if(servico == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }

        servico.setStatus(StatusServico.INATIVO);

        return this.repository.save(servico);
    }

    // ---delete---
    public void deleteServico(Long id){
        ServicoEntity servico = this.repository.findById(id).orElse(null);
        if(servico == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Serviço não encontrado!");
        }
        //INSERIR VALIDACOES DE DEPENDENCIAS

        this.repository.delete(servico);
    }
}
