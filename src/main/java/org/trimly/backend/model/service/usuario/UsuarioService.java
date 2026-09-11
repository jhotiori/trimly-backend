package org.trimly.backend.model.service.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.EntityNotFoundException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.UsuarioRepository;
import org.trimly.backend.view.dto.usuario.UsuarioCreateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;
import org.trimly.backend.view.mapper.UsuarioMapper;

import lombok.RequiredArgsConstructor;

/**
 * Serviço de usuários. Aplica BCrypt à senha, valida a unicidade do e-mail e as reatribuições de
 * cargo via {@link UsuarioValidator} e orquestra a criação, atualização, consulta e remoção.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {
    /**
     * Repositório de usuários.
     * @see {@link UsuarioRepository}
     */
    private final UsuarioRepository repository;

    /**
     * Mapper de usuários.
     * @see {@link UsuarioMapper}
     */
    private final UsuarioMapper mapper;

    /**
     * Codificador BCrypt aplicado à senha do usuário.
     * @see {@link PasswordEncoder}
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Validações das regras de usuário.
     * @see {@link UsuarioValidator}
     */
    private final UsuarioValidator usuarioValidator;

    /**
     * Cria um novo usuário com cargo cliente e senha criptografada após validar a unicidade do e-mail.
     *
     * @param request - dados do usuário a ser criado
     * @throws UsuarioEmailExistenteException - quando já existe um usuário com o mesmo e-mail
     * @return UsuarioEntity - o usuário criado e persistido
     */
    @Transactional
    public UsuarioEntity create(UsuarioCreateDTO request) {
        UsuarioEntity entity = mapper.toEntity(request);
        entity.setCargo(UsuarioCargo.CLIENTE);
        entity.setSenha(passwordEncoder.encode(request.senha()));

        usuarioValidator.validateEmailUnico(request.email(), null);

        entity = repository.save(entity);
        return entity;
    }

    /**
     * Atualiza os campos informados de um usuário existente, recriptografando a senha quando enviada.
     *
     * Uma requisição sem nenhum campo informado é um no-op: o usuário é retornado
     * inalterado, sem escrita nem revalidação.
     *
     * @param id - identificador do usuário a ser atualizado
     * @param request - campos a atualizar (nome, e-mail, senha e/ou cargo)
     * @throws EntityNotFoundException - quando não existe usuário com o id informado
     * @throws UsuarioEmailExistenteException - quando o e-mail informado já pertence a outro usuário
     * @throws UsuarioException - quando a reatribuição de cargo não é permitida
     * @return UsuarioEntity - o usuário atualizado
     */
    @Transactional
    public UsuarioEntity update(Long id, UsuarioUpdateDTO request) {
        UsuarioEntity entity = this.findById(id);

        if (request.nome() == null && request.email() == null && request.senha() == null && request.cargo() == null) {
            return entity;
        }

        usuarioValidator.validateCargoUpdate(entity, request.cargo());

        String nome = request.nome();
        if (nome != null && !nome.isBlank()) {
            entity.setNome(nome);
        }

        String email = request.email();
        if (email != null) {
            usuarioValidator.validateEmailUnico(email, id);
            if (!email.isBlank()) {
                entity.setEmail(email);
            }
        }

        String senha = request.senha();
        if (senha != null && !senha.isBlank()) {
            entity.setSenha(passwordEncoder.encode(senha));
        }

        UsuarioCargo cargo = request.cargo();
        if (cargo != null) {
            entity.setCargo(cargo);
        }

        entity = repository.save(entity);
        return entity;
    }

    /**
     * Retorna todos os usuários cadastrados.
     *
     * @return List - lista de todos os usuários
     */
    public List<UsuarioEntity> findAll() {
        return repository.findAll();
    }

    /**
     * Busca um usuário pelo seu identificador.
     *
     * @param id - identificador do usuário
     * @throws EntityNotFoundException - quando não existe usuário com o id informado
     * @return UsuarioEntity - o usuário encontrado
     */
    public UsuarioEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não foi encontrado"));
    }

    /**
     * Retorna os usuários cujo nome corresponde ao termo informado.
     *
     * @param nome - termo de busca aplicado ao nome
     * @return List - lista de usuários correspondentes
     */
    public List<UsuarioEntity> findByNome(String nome) {
        return repository.findByNomeLikeIgnoreCase(nome);
    }

    /**
     * Retorna o usuário com o email informado.
     *
     * @param email - e-mail do usuário
     * @throws EntityNotFoundException - quando não existe usuário com o e-mail informado
     * @return UsuarioEntity - o usuário encontrado
     */
    public UsuarioEntity findByEmail(String email) {
        return repository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario com email não foi encontrado"));
    }

    /**
     * Retorna o usuário cujo e-mail e senha conferem com as credenciais informadas.
     *
     * Diferente dos demais métodos de busca, nunca lança exceção: um e-mail desconhecido ou uma
     * senha incorreta devolvem {@code Optional.empty()}, para que o chamador possa responder
     * {@code 200} com uma falha de login em vez de um erro de domínio.
     *
     * @param email - e-mail do usuário
     * @param senha - senha em texto puro
     * @return Optional - o usuário autenticado, ou vazio quando as credenciais não conferem
     */
    public Optional<UsuarioEntity> findByCredenciais(String email, String senha) {
        return repository.findByEmail(email).filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()));
    }

    /**
     * Remove o usuário com o identificador informado.
     *
     * @param id - identificador do usuário a ser removido
     * @throws EntityNotFoundException - quando não existe usuário com o id informado
     * @throws UsuarioComAgendamentoPendenteException - quando o usuário possui agendamento em {@code AGENDADO}
     */
    @Transactional
    public void deleteById(Long id) {
        UsuarioEntity entity = this.findById(id);
        usuarioValidator.validateSemAgendamentoPendente(id);
        repository.delete(entity);
    }
}
