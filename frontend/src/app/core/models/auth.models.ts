export type Rol = 'ADMIN' | 'ARQUITECTO' | 'COLABORADOR';

export interface Usuario {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: Rol;
  organizacionId: number;
  organizacionNombre: string;
  activo: boolean;
}

export interface AuthResponse {
  token: string;
  usuario: Usuario;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistroRequest {
  nombreOrganizacion: string;
  cuit: string;
  nombre: string;
  apellido: string;
  email: string;
  password: string;
}

export interface ErrorResponse {
  status: number;
  mensaje: string;
  errores: Record<string, string> | null;
  timestamp: string;
}