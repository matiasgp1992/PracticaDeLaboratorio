import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente, ClienteRequest, FiltrosCliente } from '../models/cliente.models';
import { Pagina } from '../models/pagina.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/clientes';

  buscar(filtros: FiltrosCliente): Observable<Pagina<Cliente>> {
    let params = new HttpParams()
      .set('pagina', filtros.pagina)
      .set('tamanio', filtros.tamanio);

    if (filtros.texto.trim()) {
      params = params.set('texto', filtros.texto.trim());
    }
    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    if (filtros.incluirInactivos) {
      params = params.set('incluirInactivos', true);
    }

    return this.http.get<Pagina<Cliente>>(this.url, { params });
  }

  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.url}/${id}`);
  }

  crear(datos: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.url, datos);
  }

  actualizar(id: number, datos: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.url}/${id}`, datos);
  }

  darDeBaja(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  reactivar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.url}/${id}/reactivar`, {});
  }
}