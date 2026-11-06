import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegistroRequest, Usuario } from '../models/auth.models';

const TOKEN_KEY = 'token';
const USUARIO_KEY = 'usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly usuario = signal<Usuario | null>(this.leerSesion());
  readonly estaLogueado = computed(() => this.usuario() !== null);
  readonly esAdmin = computed(() => this.usuario()?.rol === 'ADMIN');
  readonly puedeGestionar = computed(() => {
    const rol = this.usuario()?.rol;
    return rol === 'ADMIN' || rol === 'ARQUITECTO';
  });

  login(datos: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', datos).pipe(
      tap(respuesta => this.guardarSesion(respuesta))
    );
  }

  registrar(datos: RegistroRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/registro', datos).pipe(
      tap(respuesta => this.guardarSesion(respuesta))
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USUARIO_KEY);
    this.usuario.set(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  private guardarSesion(respuesta: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, respuesta.token);
    localStorage.setItem(USUARIO_KEY, JSON.stringify(respuesta.usuario));
    this.usuario.set(respuesta.usuario);
  }

  private leerSesion(): Usuario | null {
    const token = localStorage.getItem(TOKEN_KEY);
    const usuario = localStorage.getItem(USUARIO_KEY);

    if (!token || !usuario || !this.tokenVigente(token)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USUARIO_KEY);
      return null;
    }

    return JSON.parse(usuario) as Usuario;
  }

  private tokenVigente(token: string): boolean {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const datos = JSON.parse(atob(payload));
      return datos.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
}