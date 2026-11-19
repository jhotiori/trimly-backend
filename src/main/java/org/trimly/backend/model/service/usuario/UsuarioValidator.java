package org.trimly.backend.model.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.usuario.UsuarioCargoRepetidoException;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioSenhaInvalidaException;
import org.trimly.backend.model.exception.usuario.UsuarioUnicoDonoException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.UsuarioRepository;

/**
 * Valida e-mail único, senha mínima, troca de cargo, único `DONO` e remoção sem agendamento pendente.
 */
@Component
@RequiredArgsConstructor
public class UsuarioValidator {
    /**
     * Quantidade mínima de caracteres aceita para a senha.
     */
    private static final int SENHA_TAMANHO_MINIMO = 6;

    /**
     * Repositório de usuários, usado para checar a unicidade do e-mail e a contagem de donos.
     * @see {@link UsuarioRepository}
     */
    private final UsuarioRepository repository;

    /**
     * Repositório de agendamentos, usado para checar agendamentos pendentes do usuário.
     * @see {@link AgendamentoRepository}
     */
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Verifica se o e-mail informado não está em uso por outro usuário.
     *
     * @param email - e-mail a ser verificado
     * @param idExcluido - identificador do usuário a desconsiderar na verificação, ou nulo
     * @throws UsuarioEmailExistenteException - quando o e-mail já pertence a outro usuário
     */
    public void validateEmailUnico(String email, Long idExcluido) {
        boolean emUso = idExcluido == null
                ? repository.existsByEmail(email)
                : repository.existsByEmailAndIdNot(email, idExcluido);
        if (emUso) {
            throw new UsuarioEmailExistenteException();
        }
    }

    /**
     * Verifica se a senha informada tem o comprimento mínimo exigido.
     *
     * @param senha - senha em texto puro a ser verificada
     * @throws UsuarioSenhaInvalidaException - quando a senha é nula ou tem menos de 6 caracteres
     */
    public void validateSenha(String senha) {
        if (senha == null || senha.length() < SENHA_TAMANHO_MINIMO) {
            throw new UsuarioSenhaInvalidaException();
        }
    }

    /**
     * Verifica se a reatribuição de cargo solicitada é permitida.
     *
     * @param usuario - usuário a ter o cargo alterado
     * @param novo - cargo desejado, ou `null` quando o cargo não está sendo alterado
     * @throws UsuarioCargoRepetidoException - quando o cargo é igual ao atual
     * @throws UsuarioUnicoDonoException - quando a mudança remove o único usuário com cargo `DONO`
     */
    public void validateCargoUpdate(UsuarioEntity usuario, UsuarioCargo novo) {
        if (novo == null) {
            return;
        }
        UsuarioCargo atual = usuario.getCargo();
        if (atual == novo) {
            throw new UsuarioCargoRepetidoException();
        }
        if (atual == UsuarioCargo.DONO && repository.countByCargo(UsuarioCargo.DONO) == 1) {
            throw new UsuarioUnicoDonoException();
        }
    }

    /**
     * Verifica se o usuário não possui agendamento pendente.
     *
     * @param usuarioId - identificador do usuário a ser removido
     * @throws UsuarioComAgendamentoPendenteException - quando o usuário possui agendamento em `AGENDADO`
     */
    public void validateSemAgendamentoPendente(Long usuarioId) {
        if (agendamentoRepository.existsByUsuarioIdAndStatus(usuarioId, AgendamentoStatus.AGENDADO)) {
            throw new UsuarioComAgendamentoPendenteException();
        }
    }
}
