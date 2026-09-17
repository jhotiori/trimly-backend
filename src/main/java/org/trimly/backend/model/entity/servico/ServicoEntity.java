package org.trimly.backend.model.entity.servico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Serviço oferecido pela barbearia, com nome único, valor, duração em minutos e status de disponibilidade.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Servico")
@Table(name = "servicos")
public class ServicoEntity {
    /**
     * Identificador do serviço.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do serviço, único no catálogo.
     */
    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    /**
     * Valor cobrado pelo serviço.
     */
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /**
     * Duração do atendimento em minutos.
     */
    @Column(name = "duracao", nullable = false)
    private Integer duracao;

    /**
     * Status do serviço no catálogo (ativo ou inativo).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ServicoStatus status;
}
