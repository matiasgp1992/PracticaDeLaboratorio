package com.example.practicalaboratorio.proyecto.repository;

import com.example.practicalaboratorio.proyecto.entity.HistorialEtapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEtapaRepository extends JpaRepository<HistorialEtapa, Long> {

    List<HistorialEtapa> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);
}