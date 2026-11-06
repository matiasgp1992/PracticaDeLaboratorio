package com.example.practicalaboratorio.proyecto.repository;

import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import com.example.practicalaboratorio.proyecto.entity.EtapaProyecto;
import com.example.practicalaboratorio.proyecto.entity.Proyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    Optional<Proyecto> findByIdAndOrganizacionId(Long id, Long organizacionId);

    boolean existsByClienteIdAndEstadoIn(Long clienteId, Collection<EstadoProyecto> estados);

    @Query(value = """
            SELECT p FROM Proyecto p
            JOIN FETCH p.cliente c
            WHERE p.organizacion.id = :organizacionId
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:etapa IS NULL OR p.etapaActual = :etapa)
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:usuarioId IS NULL OR EXISTS (
                    SELECT m.id FROM MiembroProyecto m
                    WHERE m.proyecto = p AND m.usuario.id = :usuarioId))
              AND (:texto IS NULL
                   OR LOWER(p.nombre) LIKE :texto
                   OR LOWER(p.direccionObra) LIKE :texto
                   OR LOWER(c.razonSocial) LIKE :texto
                   OR LOWER(c.apellido) LIKE :texto
                   OR LOWER(c.nombre) LIKE :texto)
            ORDER BY p.fechaAlta DESC
            """,
            countQuery = """
            SELECT COUNT(p) FROM Proyecto p
            JOIN p.cliente c
            WHERE p.organizacion.id = :organizacionId
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:etapa IS NULL OR p.etapaActual = :etapa)
              AND (:estado IS NULL OR p.estado = :estado)
              AND (:usuarioId IS NULL OR EXISTS (
                    SELECT m.id FROM MiembroProyecto m
                    WHERE m.proyecto = p AND m.usuario.id = :usuarioId))
              AND (:texto IS NULL
                   OR LOWER(p.nombre) LIKE :texto
                   OR LOWER(p.direccionObra) LIKE :texto
                   OR LOWER(c.razonSocial) LIKE :texto
                   OR LOWER(c.apellido) LIKE :texto
                   OR LOWER(c.nombre) LIKE :texto)
            """)
    Page<Proyecto> buscar(@Param("organizacionId") Long organizacionId,
                          @Param("texto") String texto,
                          @Param("clienteId") Long clienteId,
                          @Param("etapa") EtapaProyecto etapa,
                          @Param("estado") EstadoProyecto estado,
                          @Param("usuarioId") Long usuarioId,
                          Pageable pageable);
}