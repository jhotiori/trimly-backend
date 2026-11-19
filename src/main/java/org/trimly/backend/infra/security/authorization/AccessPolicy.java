package org.trimly.backend.infra.security.authorization;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.trimly.backend.model.entity.agendamento.AgendamentoStatus;
import org.trimly.backend.model.entity.usuario.UsuarioCargo;
import org.trimly.backend.model.entity.usuario.UsuarioEntity;
import org.trimly.backend.model.repository.AgendamentoRepository;
import org.trimly.backend.view.dto.agendamento.AgendamentoFilter;
import org.trimly.backend.view.dto.agendamento.AgendamentoUpdateDTO;
import org.trimly.backend.view.dto.usuario.UsuarioUpdateDTO;

/**
 * Regras de posse usadas em `@PreAuthorize`: `DONO` e `ADMIN` passam direto, `CLIENTE` só acessa os próprios dados.
 *
 * Os métodos nunca lançam exceção: um id inexistente retorna `false` para o `CLIENTE`.
 */
@Component("accessPolicy")
@RequiredArgsConstructor
public class AccessPolicy {
    /**
     * Cargos com acesso total.
     */
    private static final Set<UsuarioCargo> STAFF = EnumSet.of(UsuarioCargo.DONO, UsuarioCargo.ADMIN);

    /**
     * Repositório de agendamentos, usado para conferir o dono.
     * @see {@link AgendamentoRepository}
     */
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Confere se o usuário autenticado é staff ou o próprio usuário informado.
     *
     * @param usuarioId - identificador do usuário acessado
     * @return boolean - `true` quando o acesso é permitido
     */
    public boolean isStaffOrSelf(Long usuarioId) {
        return isStaff() || isSelf(usuarioId);
    }

    /**
     * Confere se o usuário autenticado é staff ou dono do agendamento.
     *
     * @param id - identificador do agendamento
     * @return boolean - `true` quando o acesso é permitido
     */
    public boolean canAccessAgendamento(Long id) {
        return isStaff() || isOwner(id);
    }

    /**
     * Confere se o usuário autenticado pode atualizar o agendamento. O `CLIENTE` dono só pode remarcar ou definir
     * o status `CANCELADO`.
     *
     * @param id - identificador do agendamento
     * @param request - campos a atualizar
     * @return boolean - `true` quando a atualização é permitida
     */
    public boolean canUpdateAgendamento(Long id, AgendamentoUpdateDTO request) {
        if (isStaff()) {
            return true;
        }
        AgendamentoStatus status = request.status();
        return (status == null || status == AgendamentoStatus.CANCELADO) && isOwner(id);
    }

    /**
     * Confere se o usuário autenticado pode atualizar o usuário. O `CLIENTE` só atualiza a si mesmo e sem `cargo`.
     *
     * @param id - identificador do usuário
     * @param request - campos a atualizar
     * @return boolean - `true` quando a atualização é permitida
     */
    public boolean canUpdateUsuario(Long id, UsuarioUpdateDTO request) {
        return isStaff() || (request.cargo() == null && isSelf(id));
    }

    /**
     * Restringe o filtro de agendamentos aos do próprio `CLIENTE`, ignorando o `usuarioId` informado. Staff recebe o
     * filtro inalterado.
     *
     * @param filtro - filtro informado na requisição
     * @throws AccessDeniedException - quando não há usuário autenticado
     * @return AgendamentoFilter - o filtro a aplicar
     */
    public AgendamentoFilter restrictFilter(AgendamentoFilter filtro) {
        if (isStaff()) {
            return filtro;
        }
        Long usuarioId = currentUsuario().map(UsuarioEntity::getId)
                .orElseThrow(() -> new AccessDeniedException("Acesso negado"));
        return new AgendamentoFilter(filtro.status(), filtro.data(), usuarioId, filtro.servicoId());
    }

    /**
     * Confere se o usuário autenticado tem cargo de staff.
     *
     * @return boolean - `true` para `DONO` e `ADMIN`
     */
    private boolean isStaff() {
        return currentUsuario().map(usuario -> STAFF.contains(usuario.getCargo())).orElse(false);
    }

    /**
     * Confere se o usuário autenticado é o usuário informado.
     *
     * @param usuarioId - identificador do usuário
     * @return boolean - `true` quando os ids coincidem
     */
    private boolean isSelf(Long usuarioId) {
        return currentUsuario().map(usuario -> usuario.getId().equals(usuarioId)).orElse(false);
    }

    /**
     * Confere se o usuário autenticado é dono do agendamento.
     *
     * @param agendamentoId - identificador do agendamento
     * @return boolean - `true` quando o agendamento existe e pertence ao usuário
     */
    private boolean isOwner(Long agendamentoId) {
        return agendamentoId != null && agendamentoRepository.findById(agendamentoId)
                .map(agendamento -> isSelf(agendamento.getUsuario().getId()))
                .orElse(false);
    }

    /**
     * Lê o usuário autenticado do `SecurityContext`.
     *
     * @return Optional - o usuário autenticado, ou vazio quando a requisição é anônima
     */
    private Optional<UsuarioEntity> currentUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UsuarioEntity usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }
}
