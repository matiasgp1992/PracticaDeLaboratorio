import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Solo usuarios logueados. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaLogueado() ? true : inject(Router).createUrlTree(['/login']);
};

/** Solo usuarios con rol ADMIN. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.esAdmin() ? true : inject(Router).createUrlTree(['/inicio']);
};

/** Solo usuarios NO logueados (login y registro). */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estaLogueado() ? inject(Router).createUrlTree(['/inicio']) : true;
};

/** Solo ADMIN o ARQUITECTO (alta y edición de clientes y proyectos). */
export const gestionGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.puedeGestionar() ? true : inject(Router).createUrlTree(['/inicio']);
};