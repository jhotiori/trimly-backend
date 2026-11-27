package org.trimly.backend.model.entity.recesso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Período em que a barbearia não atende, inclusivo nas duas pontas.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Recesso")
@Table(name = "recessos")
public class RecessoEntity {
    /**
     * Identificador do recesso.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descrição do recesso.
     */
    @Column(nullable = false, length = 128)
    private String descricao;

    /**
     * Primeiro dia do recesso.
     */
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    /**
     * Último dia do recesso.
     */
    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;
}
