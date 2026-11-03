import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from '../models/auth.models';

export function mensajeDeError(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor. Verificá que el backend esté corriendo.';
  }

  const cuerpo = error.error as ErrorResponse | null;

  if (cuerpo?.errores) {
    return Object.values(cuerpo.errores).join('. ');
  }

  return cuerpo?.mensaje ?? 'Ocurrió un error inesperado.';
}