package com.example.practicalaboratorio.proyecto.repository;

import com.example.practicalaboratorio.proyecto.entity.MiembroProyecto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MiembroProyectoRepository extends JpaRepository<MiembroProyecto, Long> {

    List<MiembroProyecto> findByProyectoIdOrderByFechaAsignacionAsc(Long proyectoId);

    boolean existsByProyectoIdAndUsuarioId(Long proyectoId, Long usuarioId);

    Optional<MiembroProyecto> findByProyectoIdAndUsuarioId(Long proyectoId, Long usuarioId);
}