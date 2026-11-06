package com.example.practicalaboratorio.proyecto.entity;

import com.example.practicalaboratorio.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "miembros_proyecto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_miembro_proyecto_usuario",
                columnNames = {"proyecto_id", "usuario_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class MiembroProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String funcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @PrePersist
    void prePersist() {
        this.fechaAsignacion = LocalDateTime.now();
    }
}