package com.example.practicalaboratorio.cliente.repository;

import com.example.practicalaboratorio.cliente.entity.Cliente;
import com.example.practicalaboratorio.cliente.entity.TipoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndOrganizacionId(Long id, Long organizacionId);

    boolean existsByOrganizacionIdAndDocumento(Long organizacionId, String documento);

    boolean existsByOrganizacionIdAndDocumentoAndIdNot(Long organizacionId, String documento, Long id);

    @Query(value = """
            SELECT c FROM Cliente c
            WHERE c.organizacion.id = :organizacionId
              AND (:incluirInactivos = true OR c.activo = true)
              AND (:tipo IS NULL OR c.tipo = :tipo)
              AND (:texto IS NULL
                   OR LOWER(c.nombre) LIKE :texto
                   OR LOWER(c.apellido) LIKE :texto
                   OR LOWER(c.razonSocial) LIKE :texto
                   OR LOWER(c.documento) LIKE :texto
                   OR LOWER(c.email) LIKE :texto)
            ORDER BY COALESCE(c.razonSocial, c.apellido), c.nombre
            """,
            countQuery = """
            SELECT COUNT(c) FROM Cliente c
            WHERE c.organizacion.id = :organizacionId
              AND (:incluirInactivos = true OR c.activo = true)
              AND (:tipo IS NULL OR c.tipo = :tipo)
              AND (:texto IS NULL
                   OR LOWER(c.nombre) LIKE :texto
                   OR LOWER(c.apellido) LIKE :texto
                   OR LOWER(c.razonSocial) LIKE :texto
                   OR LOWER(c.documento) LIKE :texto
                   OR LOWER(c.email) LIKE :texto)
            """)
    Page<Cliente> buscar(@Param("organizacionId") Long organizacionId,
                         @Param("texto") String texto,
                         @Param("tipo") TipoCliente tipo,
                         @Param("incluirInactivos") boolean incluirInactivos,
                         Pageable pageable);
}