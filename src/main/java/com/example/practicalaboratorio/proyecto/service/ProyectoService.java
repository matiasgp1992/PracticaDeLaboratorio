package com.example.practicalaboratorio.proyecto.service;

import com.example.practicalaboratorio.auth.security.UsuarioAutenticado;
import com.example.practicalaboratorio.cliente.entity.Cliente;
import com.example.practicalaboratorio.cliente.repository.ClienteRepository;
import com.example.practicalaboratorio.common.dto.PaginaResponse;
import com.example.practicalaboratorio.common.exception.DatosInvalidosException;
import com.example.practicalaboratorio.common.exception.RecursoNoEncontradoException;
import com.example.practicalaboratorio.common.exception.ReglaNegocioException;
import com.example.practicalaboratorio.organizacion.repository.OrganizacionRepository;
import com.example.practicalaboratorio.proyecto.dto.*;
import com.example.practicalaboratorio.proyecto.entity.*;
import com.example.practicalaboratorio.proyecto.repository.HistorialEtapaRepository;
import com.example.practicalaboratorio.proyecto.repository.MiembroProyectoRepository;
import com.example.practicalaboratorio.proyecto.repository.ProyectoRepository;
import com.example.practicalaboratorio.usuario.entity.Usuario;
import com.example.practicalaboratorio.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final MiembroProyectoRepository miembroRepository;
    private final HistorialEtapaRepository historialRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrganizacionRepository organizacionRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<ProyectoResponse> buscar(String texto, Long clienteId, EtapaProyecto etapa,
                                                   EstadoProyecto estado, int pagina, int tamanio,
                                                   UsuarioAutenticado actual) {
        String filtro = (texto == null || texto.isBlank()) ? null : "%" + texto.trim().toLowerCase() + "%";
        Long soloMiembro = actual.puedeGestionar() ? null : actual.id();
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanio, 1), 100));

        return PaginaResponse.desde(
                proyectoRepository.buscar(actual.organizacionId(), filtro, clienteId, etapa, estado, soloMiembro, pageable)
                        .map(ProyectoResponse::desde)
        );
    }

    @Transactional(readOnly = true)
    public ProyectoDetalleResponse obtener(Long id, UsuarioAutenticado actual) {
        return armarDetalle(buscarConAcceso(id, actual));
    }

    @Transactional
    public ProyectoDetalleResponse crear(ProyectoRequest request, UsuarioAutenticado actual) {
        validarFechas(request);
        Cliente cliente = buscarClienteActivo(request.clienteId(), actual);
        Usuario creador = usuarioRepository.getReferenceById(actual.id());

        Proyecto proyecto = new Proyecto();
        aplicarDatos(proyecto, request, cliente);
        proyecto.setEstado(request.estado() != null ? request.estado() : EstadoProyecto.EN_CURSO);
        proyecto.setOrganizacion(organizacionRepository.getReferenceById(actual.organizacionId()));
        proyectoRepository.save(proyecto);

        registrarHistorial(proyecto, null, proyecto.getEtapaActual(), creador, "Alta del proyecto");

        MiembroProyecto responsable = new MiembroProyecto();
        responsable.setProyecto(proyecto);
        responsable.setUsuario(creador);
        responsable.setFuncion("Responsable");
        miembroRepository.save(responsable);

        return armarDetalle(proyecto);
    }

    @Transactional
    public ProyectoDetalleResponse actualizar(Long id, ProyectoRequest request, UsuarioAutenticado actual) {
        validarFechas(request);
        Proyecto proyecto = buscarEnMiOrganizacion(id, actual);

        Cliente cliente = Objects.equals(proyecto.getCliente().getId(), request.clienteId())
                ? proyecto.getCliente()
                : buscarClienteActivo(request.clienteId(), actual);

        aplicarDatos(proyecto, request, cliente);
        if (request.estado() != null) {
            proyecto.setEstado(request.estado());
        }

        return armarDetalle(proyecto);
    }

    @Transactional
    public ProyectoDetalleResponse cambiarEtapa(Long id, CambiarEtapaRequest request, UsuarioAutenticado actual) {
        Proyecto proyecto = buscarConAcceso(id, actual);

        if (proyecto.getEstado() != EstadoProyecto.EN_CURSO) {
            throw new ReglaNegocioException("Solo se puede cambiar la etapa de un proyecto en curso");
        }
        if (proyecto.getEtapaActual() == request.etapa()) {
            throw new ReglaNegocioException("El proyecto ya se encuentra en esa etapa");
        }

        EtapaProyecto anterior = proyecto.getEtapaActual();
        proyecto.setEtapaActual(request.etapa());

        String observacion = (request.observacion() == null || request.observacion().isBlank())
                ? null : request.observacion().trim();
        registrarHistorial(proyecto, anterior, request.etapa(),
                usuarioRepository.getReferenceById(actual.id()), observacion);

        return armarDetalle(proyecto);
    }

    @Transactional
    public ProyectoDetalleResponse agregarMiembro(Long id, AgregarMiembroRequest request, UsuarioAutenticado actual) {
        Proyecto proyecto = buscarEnMiOrganizacion(id, actual);

        Usuario usuario = usuarioRepository.findByIdAndOrganizacionId(request.usuarioId(), actual.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un usuario inactivo");
        }
        if (miembroRepository.existsByProyectoIdAndUsuarioId(proyecto.getId(), usuario.getId())) {
            throw new ReglaNegocioException("El usuario ya es miembro del proyecto");
        }

        MiembroProyecto miembro = new MiembroProyecto();
        miembro.setProyecto(proyecto);
        miembro.setUsuario(usuario);
        miembro.setFuncion(request.funcion().trim());
        miembroRepository.save(miembro);

        return armarDetalle(proyecto);
    }

    @Transactional
    public ProyectoDetalleResponse quitarMiembro(Long id, Long usuarioId, UsuarioAutenticado actual) {
        Proyecto proyecto = buscarEnMiOrganizacion(id, actual);

        MiembroProyecto miembro = miembroRepository.findByProyectoIdAndUsuarioId(proyecto.getId(), usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no es miembro del proyecto"));
        miembroRepository.delete(miembro);

        return armarDetalle(proyecto);
    }

    // ---------- Métodos auxiliares ----------

    private Proyecto buscarEnMiOrganizacion(Long id, UsuarioAutenticado actual) {
        return proyectoRepository.findByIdAndOrganizacionId(id, actual.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
    }

    /** Igual que buscarEnMiOrganizacion, pero un COLABORADOR solo accede si es miembro. */
    private Proyecto buscarConAcceso(Long id, UsuarioAutenticado actual) {
        Proyecto proyecto = buscarEnMiOrganizacion(id, actual);
        if (!actual.puedeGestionar() && !miembroRepository.existsByProyectoIdAndUsuarioId(id, actual.id())) {
            throw new RecursoNoEncontradoException("Proyecto no encontrado");
        }
        return proyecto;
    }

    private Cliente buscarClienteActivo(Long clienteId, UsuarioAutenticado actual) {
        Cliente cliente = clienteRepository.findByIdAndOrganizacionId(clienteId, actual.organizacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un cliente dado de baja");
        }
        return cliente;
    }

    private void validarFechas(ProyectoRequest request) {
        if (request.fechaInicio() != null && request.fechaFinEstimada() != null
                && request.fechaFinEstimada().isBefore(request.fechaInicio())) {
            throw new DatosInvalidosException(Map.of(
                    "fechaFinEstimada", "La fecha de fin estimada no puede ser anterior a la de inicio"));
        }
    }

    private void aplicarDatos(Proyecto proyecto, ProyectoRequest request, Cliente cliente) {
        proyecto.setNombre(request.nombre().trim());
        proyecto.setDescripcion(limpiar(request.descripcion()));
        proyecto.setDireccionObra(limpiar(request.direccionObra()));
        proyecto.setCliente(cliente);
        proyecto.setFechaInicio(request.fechaInicio());
        proyecto.setFechaFinEstimada(request.fechaFinEstimada());
    }

    private void registrarHistorial(Proyecto proyecto, EtapaProyecto anterior, EtapaProyecto nueva,
                                    Usuario usuario, String observacion) {
        HistorialEtapa historial = new HistorialEtapa();
        historial.setProyecto(proyecto);
        historial.setEtapaAnterior(anterior);
        historial.setEtapaNueva(nueva);
        historial.setUsuario(usuario);
        historial.setObservacion(observacion);
        historialRepository.save(historial);
    }

    private ProyectoDetalleResponse armarDetalle(Proyecto proyecto) {
        var miembros = miembroRepository.findByProyectoIdOrderByFechaAsignacionAsc(proyecto.getId())
                .stream().map(MiembroResponse::desde).toList();
        var historial = historialRepository.findByProyectoIdOrderByFechaDescIdDesc(proyecto.getId())
                .stream().map(HistorialEtapaResponse::desde).toList();
        return ProyectoDetalleResponse.desde(proyecto, miembros, historial);
    }

    private static String limpiar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}