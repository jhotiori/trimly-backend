package org.trimly.backend.model.entity.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuário do sistema, identificado por e-mail único e senha criptografada, com um cargo que define seu papel.
 *
 * Implementa {@link UserDetails} para integrar com o Spring Security: as autoridades derivam do cargo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Usuario")
@Table(name = "usuarios")
public class UsuarioEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo", nullable = false)
    private UsuarioCargo cargo;

    /**
     * Deriva a única autoridade do usuário do seu cargo, com o prefixo {@code ROLE_}.
     *
     * @return Collection - a autoridade correspondente ao cargo
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.getCargo().toString()));
    }

    /**
     * Expõe a senha criptografada para o Spring Security.
     *
     * @return String - a senha armazenada
     */
    @Override
    public String getPassword() {
        return this.getSenha();
    }

    /**
     * Usa o e-mail como nome de usuário do Spring Security.
     *
     * @return String - o e-mail do usuário
     */
    @Override
    public String getUsername() {
        return this.getEmail();
    }

    /**
     * Informa que a conta nunca expira.
     *
     * @return boolean - sempre {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Informa que a conta nunca é bloqueada.
     *
     * @return boolean - sempre {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Informa que as credenciais nunca expiram.
     *
     * @return boolean - sempre {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Informa que a conta está sempre habilitada.
     *
     * @return boolean - sempre {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
