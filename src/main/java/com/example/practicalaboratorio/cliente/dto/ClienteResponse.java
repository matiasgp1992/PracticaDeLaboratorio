package com.example.practicalaboratorio.cliente.dto;

import com.example.practicalaboratorio.cliente.entity.Cliente;
import com.example.practicalaboratorio.cliente.entity.TipoCliente;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        TipoCliente tipo,
        String nombre,
        String apellido,
        String razonSocial,
        String denominacion,
        String documento,
        String email,
        String telefono,
        String direccion,
        boolean activo,
        LocalDateTime fechaAlta
) {
    public static ClienteResponse desde(Cliente cliente) {
        String denominacion = cliente.getDenominacion();

        return new ClienteResponse(
                cliente.getId(),
                cliente.getTipo(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getRazonSocial(),
                denominacion,
                cliente.getDocumento(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.isActivo(),
                cliente.getFechaAlta()
        );
    }
}