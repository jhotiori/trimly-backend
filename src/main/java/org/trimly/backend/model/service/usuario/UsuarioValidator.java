package org.trimly.backend.model.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.exception.usuario.UsuarioComAgendamentoPendenteException;
import org.trimly.backend.model.exception.usuario.UsuarioEmailExistenteException;
import org.trimly.backend.model.exception.usuario.UsuarioException;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.model.repository.UsuarioRepository;

/**
 * Validador de usuários. Garante a unicidade do e-mail, a legalidade da reatribuição de cargo, a
 * preservação do único {@code DONO} cadastrado e a ausência de agendamento pendente na remoção.
 */
@Component
@RequiredArgsConstructor
public class UsuarioValidator {
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
            throw new UsuarioEmailExistenteException("Já existe um usuário com esse e-mail");
        }
    }

    /**
     * Verifica se a reatribuição de cargo solicitada é permitida.
     *
     * @param usuario - usuário a ter o cargo alterado
     * @param novo - cargo desejado, ou {@code null} quando o cargo não está sendo alterado
     * @throws UsuarioException - quando o cargo é igual ao atual, ou quando a mudança remove o único usuário com cargo {@code DONO}
     */
    public void validateCargoUpdate(UsuarioEntity usuario, UsuarioCargo novo) {
        if (novo == null) {
            return;
        }
        UsuarioCargo atual = usuario.getCargo();
        if (atual == novo) {
            throw new UsuarioException("O usuário já possui esse cargo");
        }
        if (atual == UsuarioCargo.DONO && repository.countByCargo(UsuarioCargo.DONO) == 1) {
            throw new UsuarioException("Não é possível remover o cargo DONO do único dono cadastrado");
        }
    }

    /**
     * Verifica se o usuário não possui agendamento pendente.
     *
     * @param usuarioId - identificador do usuário a ser removido
     * @throws UsuarioComAgendamentoPendenteException - quando o usuário possui agendamento em {@code AGENDADO}
     */
    public void validateSemAgendamentoPendente(Long usuarioId) {
        if (agendamentoRepository.existsByUsuarioIdAndStatus(usuarioId, AgendamentoStatus.AGENDADO)) {
            throw new UsuarioComAgendamentoPendenteException(
                    "Não é possível remover um usuário com agendamento pendente");
        }
    }
}
