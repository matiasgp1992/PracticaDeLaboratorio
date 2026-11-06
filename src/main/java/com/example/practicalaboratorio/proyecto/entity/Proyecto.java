package com.example.practicalaboratorio.proyecto.entity;

import com.example.practicalaboratorio.cliente.entity.Cliente;
import com.example.practicalaboratorio.organizacion.entity.Organizacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proyectos")
@Getter
@Setter
@NoArgsConstructor
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 200)
    private String direccionObra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EtapaProyecto etapaActual = EtapaProyecto.ANTEPROYECTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProyecto estado = EstadoProyecto.EN_CURSO;

    private LocalDate fechaInicio;

    private LocalDate fechaFinEstimada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id", nullable = false)
    private Organizacion organizacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @PrePersist
    void prePersist() {
        this.fechaAlta = LocalDateTime.now();
    }
}