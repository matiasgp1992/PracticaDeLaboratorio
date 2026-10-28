package com.example.practicalaboratorio.organizacion.repository;

import com.example.practicalaboratorio.organizacion.entity.Organizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizacionRepository extends JpaRepository<Organizacion, Long> {

    boolean existsByCuit(String cuit);
}