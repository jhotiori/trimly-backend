package org.trimly.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.trimly.backend.dto.disponibilidade.CreateDisponibilidadeDTO;
import org.trimly.backend.entity.DisponibilidadeEntity;
import org.trimly.backend.repository.DisponibilidadeRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {

    private final DisponibilidadeRepository repository;

    // ---exists---

    // ---find---
    public DisponibilidadeEntity findById(Long id) {
        return this.repository.findById(id).orElse(null);
    }

    // ---list---
    public List<DisponibilidadeEntity> findByDiaSemana(DayOfWeek diaSemana) {
        return this.repository.findByDiaSemana(diaSemana);
    }

    public List<DisponibilidadeEntity> listAll() {
        return this.repository.findAll();
    }

    // ---save---
    public DisponibilidadeEntity save(CreateDisponibilidadeDTO dto) {
        DisponibilidadeEntity disponibilidade = new DisponibilidadeEntity();
        disponibilidade.setDiaSemana(dto.diaSemana());
        disponibilidade.setHoraInicio(dto.horaInicio());
        disponibilidade.setHoraFim(dto.horaFim());

        return this.repository.save(disponibilidade);
    }

    // ---update---
    public DisponibilidadeEntity update(CreateDisponibilidadeDTO dto, Long id) {
        DisponibilidadeEntity disponibilidade = this.repository.findById(id).orElse(null);
        if(disponibilidade == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Disponibilidade não encontrada!");
        }

        disponibilidade.setDiaSemana(dto.diaSemana());
        disponibilidade.setHoraInicio(dto.horaInicio());
        disponibilidade.setHoraFim(dto.horaFim());

        return this.repository.save(disponibilidade);
    }

    // ---partial update---
    public DisponibilidadeEntity partialUpdate(CreateDisponibilidadeDTO dto, Long id) {
        DisponibilidadeEntity disponibilidade = this.repository.findById(id).orElse(null);
        if(disponibilidade == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Disponibilidade não encontrada!");
        }

        if(dto.diaSemana() != null) disponibilidade.setDiaSemana(dto.diaSemana());
        if(dto.horaInicio() != null) disponibilidade.setHoraInicio(dto.horaInicio());
        if(dto.horaFim() != null) disponibilidade.setHoraFim(dto.horaFim());

        return this.repository.save(disponibilidade);
    }


    // ---delete---
    public void deleteDisponibilidade(Long id) {
        DisponibilidadeEntity disponibilidade = this.repository.findById(id).orElse(null);
        if(disponibilidade == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Disponibilidade não encontrada!");
        }

        //INSERIR VALIDACOES DE DEPENENCIAS

        this.repository.delete(disponibilidade);
    }

}
