import { Routes } from '@angular/router';
import { adminGuard, authGuard, invitadoGuard } from './core/guards/auth.guards';
import { Login } from './features/auth/login/login';
import { Registro } from './features/auth/registro/registro';
import { Layout } from './features/layout/layout';
import { Inicio } from './features/inicio/inicio';
import { Usuarios } from './features/usuarios/usuarios';

export const routes: Routes = [
  { path: 'login', component: Login, canActivate: [invitadoGuard] },
  { path: 'registro', component: Registro, canActivate: [invitadoGuard] },
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    children: [
      { path: 'inicio', component: Inicio },
      { path: 'usuarios', component: Usuarios, canActivate: [adminGuard] },
      { path: '', redirectTo: 'inicio', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];