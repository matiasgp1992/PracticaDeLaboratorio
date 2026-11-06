package com.example.practicalaboratorio.cliente.dto;

import com.example.practicalaboratorio.cliente.entity.TipoCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull(message = "El tipo de cliente es obligatorio")
        TipoCliente tipo,

        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
        String razonSocial,

        @NotBlank(message = "El documento (DNI o CUIT) es obligatorio")
        @Size(max = 13, message = "El documento no puede superar los 13 caracteres")
        String documento,

        @Email(message = "El email no es válido")
        @Size(max = 150)
        String email,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion
) {
}