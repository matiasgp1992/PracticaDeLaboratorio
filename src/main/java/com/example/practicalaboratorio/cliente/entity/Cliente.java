package com.example.practicalaboratorio.cliente.entity;

import com.example.practicalaboratorio.organizacion.entity.Organizacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cliente_organizacion_documento",
                columnNames = {"organizacion_id", "documento"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCliente tipo;

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 13)
    private String documento;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    void prePersist() {
        this.fechaAlta = LocalDateTime.now();
    }

    public String getDenominacion() {
        return tipo == TipoCliente.PERSONA_JURIDICA
                ? razonSocial
                : apellido + ", " + nombre;
    }
}