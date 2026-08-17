package org.trimly.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.trimly.backend.dto.usuario.CreateUsuarioDTO;
import org.trimly.backend.entity.UsuarioEntity;
import org.trimly.backend.entity.enums.CargoUsuario;
import org.trimly.backend.repository.UsuarioRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository repository;

    // ---exists---
    public Boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public Boolean existsById(Long id) {
        return repository.existsById(id);
    }

    // ---find---
    public UsuarioEntity findById(Long id) {
        return this.repository.findById(id).orElse(null);
    }

    // ---list---
    public List<UsuarioEntity> findByName(String nome) {
        return (List<UsuarioEntity>) this.repository.findByNome(nome).orElse(null);
    }

    public List<UsuarioEntity> findByMail(String mail) {
        return (List<UsuarioEntity>) this.repository.findByEmail(mail).orElse(null);
    }

    public List<UsuarioEntity> findByCargo(CargoUsuario cargo) {
        return null;
    }

    public List<UsuarioEntity> listAll() {
        return this.repository.findAll();
    }

    // ---save---
    public UsuarioEntity save(CreateUsuarioDTO dto) {
        UsuarioEntity user = new UsuarioEntity();
        user.setNome(dto.nome());
        user.setEmail(dto.email());
        user.setSenha(dto.senha());
        user.setCargo(CargoUsuario.CLIENTE);

        return this.repository.save(user);
    }

    // ---update---
    public UsuarioEntity update(CreateUsuarioDTO dto, Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        user.setNome(dto.nome());
        user.setEmail(dto.email());
        user.setSenha(dto.senha());

        return this.repository.save(user);
    }

    // ---partial update---
    public UsuarioEntity partialUpdate(CreateUsuarioDTO dto, Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        if(dto.nome() != null) user.setNome(dto.nome());
        if(dto.email() != null) user.setEmail(dto.email());
        if(dto.senha() != null)user.setSenha(dto.senha());

        return this.repository.save(user);
    }

    public UsuarioEntity SetCargoCliente(Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        user.setCargo(CargoUsuario.CLIENTE);

        return this.repository.save(user);
    }

    public UsuarioEntity SetCargoAdmin(Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        user.setCargo(CargoUsuario.ADMIN);

        return this.repository.save(user);
    }

    public UsuarioEntity SetCargoDono(Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        user.setCargo(CargoUsuario.DONO);

        return this.repository.save(user);
    }

    // ---delete---
    public void deleteUsuario(Long id) {
        UsuarioEntity user = this.repository.findById(id).orElse(null);
        if(user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário não encontrado!");
        }

        //INSERIR VALIDACOES DE DEPENDENCIAS

        this.repository.delete(user);
    }
}