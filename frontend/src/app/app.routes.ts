import { Routes } from '@angular/router';
import { adminGuard, authGuard, invitadoGuard } from './core/guards/auth.guards';
import { Login } from './features/auth/login/login';
import { Registro } from './features/auth/registro/registro';
import { Layout } from './features/layout/layout';
import { Inicio } from './features/inicio/inicio';
import { Usuarios } from './features/usuarios/usuarios';
import { gestionGuard } from './core/guards/auth.guards';
import { ClienteLista } from './features/clientes/cliente-lista/cliente-lista';
import { ClienteForm } from './features/clientes/cliente-form/cliente-form';
import { ProyectoLista } from './features/proyectos/proyecto-lista/proyecto-lista';
import { ProyectoForm } from './features/proyectos/proyecto-form/proyecto-form';
import { ProyectoDetallePagina } from './features/proyectos/proyecto-detalle/proyecto-detalle';

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
      { path: 'clientes', component: ClienteLista },
      { path: 'clientes/nuevo', component: ClienteForm, canActivate: [gestionGuard] },
      { path: 'clientes/:id/editar', component: ClienteForm, canActivate: [gestionGuard] },
      { path: 'proyectos', component: ProyectoLista },
      { path: 'proyectos/nuevo', component: ProyectoForm, canActivate: [gestionGuard] },
      { path: 'proyectos/:id', component: ProyectoDetallePagina },
      { path: 'proyectos/:id/editar', component: ProyectoForm, canActivate: [gestionGuard] },
      { path: '', redirectTo: 'inicio', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '' }
];