package com.example.practicalaboratorio.cliente.service;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.cliente.dto.ClienteRequest;
import com.example.practicalaboratorio.cliente.dto.ClienteResponse;
import com.example.practicalaboratorio.cliente.entity.Cliente;
import com.example.practicalaboratorio.cliente.entity.TipoCliente;
import com.example.practicalaboratorio.cliente.repository.ClienteRepository;
import com.example.practicalaboratorio.common.dto.PaginaResponse;
import com.example.practicalaboratorio.common.exception.DatosInvalidosException;
import com.example.practicalaboratorio.common.exception.RecursoNoEncontradoException;
import com.example.practicalaboratorio.common.exception.ReglaNegocioException;
import com.example.practicalaboratorio.organizacion.repository.OrganizacionRepository;
import com.example.practicalaboratorio.proyecto.entity.EstadoProyecto;
import com.example.practicalaboratorio.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final OrganizacionRepository organizacionRepository;
    private final ProyectoRepository proyectoRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<ClienteResponse> buscar(String texto, TipoCliente tipo, boolean incluirInactivos,
                                                  int pagina, int tamanio, UsuarioAutenticado actual) {
        String filtro = limpiar(texto) == null ? null : "%" + texto.trim().toLowerCase() + "%";
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100));

        return PaginaResponse.desde(
                clienteRepository.buscar(actual.organizacionId(), filtro, tipo, incluirInactivos, pageable)
                        .map(ClienteResponse::desde)
        );
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id, UsuarioAutenticado actual) {
        return ClienteResponse.desde(buscarEnMiOrganizacion(id, actual));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request, UsuarioAutenticado actual) {
        Cliente cliente = new Cliente();
        aplicarDatos(cliente, request);

        if (clienteRepository.existsByOrganizacionIdAndDocumento(actual.organizacionId(), cliente.getDocumento())) {
            throw new ReglaNegocioException("Ya existe un cliente con ese documento");
        }

        cliente.setOrganizacion(organizacionRepository.getReferenceById(actual.organizacionId()));
        clienteRepository.save(cliente);
        return ClienteResponse.desde(cliente);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request, UsuarioAutenticado actual) {
        Cliente cliente = buscarEnMiOrganizacion(id, actual);
        aplicarDatos(cliente, request);

        if (clienteRepository.existsByOrganizacionIdAndDocumentoAndIdNot(
                actual.organizacionId(), cliente.getDocumento(), cliente.getId())) {
            throw new ReglaNegocioException("Ya existe otro cliente con ese documento");
        }

        return ClienteResponse.desde(cliente);
    }

    @Transactional
    public void darDeBaja(Long id, UsuarioAutenticado actual) {
        Cliente cliente = buscarEnMiOrganizacion(id, actual);
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("El cliente ya está dado de baja");
        }
        if (proyectoRepository.existsByClienteIdAndEstadoIn(
                id, List.of(EstadoProyecto.EN_CURSO, EstadoProyecto.PAUSADO))) {
            throw new ReglaNegocioException("No se puede dar de baja un cliente con proyectos en curso o pausados");
        }
        cliente.setActivo(false);
    }

    @Transactional
    public ClienteResponse reactivar(Long id, UsuarioAutenticado actual) {
        Cliente cliente = buscarEnMiOrganizacion(id, actual);
        cliente.setActivo(true);
        return ClienteResponse.desde(cliente);
    }

    private Cliente buscarEnMiOrganizacion(Long id, UsuarioAutenticado actual) {
        return clienteRepository.findByIdAndOrganizacionId(id, actual.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
    }

    private void aplicarDatos(Cliente cliente, ClienteRequest request) {
        String nombre = limpiar(request.nombre());
        String apellido = limpiar(request.apellido());
        String razonSocial = limpiar(request.razonSocial());

        Map<String, String> errores = new LinkedHashMap<>();

        if (request.tipo() == TipoCliente.PERSONA_FISICA) {
            if (nombre == null) errores.put("nombre", "El nombre es obligatorio para personas físicas");
            if (apellido == null) errores.put("apellido", "El apellido es obligatorio para personas físicas");
            razonSocial = null;
        } else {
            if (razonSocial == null) errores.put("razonSocial", "La razón social es obligatoria para personas jurídicas");
            nombre = null;
            apellido = null;
        }

        if (!errores.isEmpty()) {
            throw new DatosInvalidosException(errores);
        }

        String email = limpiar(request.email());

        cliente.setTipo(request.tipo());
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setRazonSocial(razonSocial);
        cliente.setDocumento(request.documento().trim());
        cliente.setEmail(email == null ? null : email.toLowerCase());
        cliente.setTelefono(limpiar(request.telefono()));
        cliente.setDireccion(limpiar(request.direccion()));
    }

    private static String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}