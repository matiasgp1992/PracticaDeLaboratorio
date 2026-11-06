import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Pagina } from '../models/pagina.model';
import {
  AgregarMiembroRequest,
  CambiarEtapaRequest,
  FiltrosProyecto,
  Proyecto,
  ProyectoDetalle,
  ProyectoRequest
} from '../models/proyecto.models';

@Injectable({ providedIn: 'root' })
export class ProyectoService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/proyectos';

  buscar(filtros: FiltrosProyecto): Observable<Pagina<Proyecto>> {
    let params = new HttpParams()
      .set('pagina', filtros.pagina)
      .set('tamanio', filtros.tamanio);

    if (filtros.texto.trim()) params = params.set('texto', filtros.texto.trim());
    if (filtros.clienteId !== null) params = params.set('clienteId', filtros.clienteId);
    if (filtros.etapa) params = params.set('etapa', filtros.etapa);
    if (filtros.estado) params = params.set('estado', filtros.estado);

    return this.http.get<Pagina<Proyecto>>(this.url, { params });
  }

  obtener(id: number): Observable<ProyectoDetalle> {
    return this.http.get<ProyectoDetalle>(`${this.url}/${id}`);
  }

  crear(datos: ProyectoRequest): Observable<ProyectoDetalle> {
    return this.http.post<ProyectoDetalle>(this.url, datos);
  }

  actualizar(id: number, datos: ProyectoRequest): Observable<ProyectoDetalle> {
    return this.http.put<ProyectoDetalle>(`${this.url}/${id}`, datos);
  }

  cambiarEtapa(id: number, datos: CambiarEtapaRequest): Observable<ProyectoDetalle> {
    return this.http.patch<ProyectoDetalle>(`${this.url}/${id}/etapa`, datos);
  }

  agregarMiembro(id: number, datos: AgregarMiembroRequest): Observable<ProyectoDetalle> {
    return this.http.post<ProyectoDetalle>(`${this.url}/${id}/miembros`, datos);
  }

  quitarMiembro(id: number, usuarioId: number): Observable<ProyectoDetalle> {
    return this.http.delete<ProyectoDetalle>(`${this.url}/${id}/miembros/${usuarioId}`);
  }
}