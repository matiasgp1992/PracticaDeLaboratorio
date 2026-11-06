import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Rol, Usuario } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { mensajeDeError } from '../../core/utils/errores';

@Component({
  selector: 'app-usuarios',
  imports: [ReactiveFormsModule],
  templateUrl: './usuarios.html'
})
export class Usuarios implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly auth = inject(AuthService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly mostrarFormulario = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly mensaje = signal<string | null>(null);
  protected readonly idUsuarioActual = computed(() => this.auth.usuario()?.id);

  protected readonly roles: { valor: Rol; etiqueta: string }[] = [
    { valor: 'ADMIN', etiqueta: 'Administrador' },
    { valor: 'ARQUITECTO', etiqueta: 'Arquitecto' },
    { valor: 'COLABORADOR', etiqueta: 'Colaborador' }
  ];

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    rol: ['ARQUITECTO' as Rol, Validators.required]
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.usuarioService.listar().subscribe({
      next: usuarios => {
        this.usuarios.set(usuarios);
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      }
    });
  }

  protected abrirFormulario(): void {
    this.form.reset();
    this.errorFormulario.set(null);
    this.mensaje.set(null);
    this.mostrarFormulario.set(true);
  }

  protected cancelar(): void {
    this.mostrarFormulario.set(false);
  }

  protected crear(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.usuarioService.crear(this.form.getRawValue()).subscribe({
      next: nuevo => {
        this.usuarios.update(lista => [...lista, nuevo]);
        this.mensaje.set(`Se dio de alta a ${nuevo.nombre} ${nuevo.apellido}.`);
        this.mostrarFormulario.set(false);
        this.guardando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.errorFormulario.set(mensajeDeError(e));
        this.guardando.set(false);
      }
    });
  }

  protected cambiarEstado(usuario: Usuario): void {
    const accion = usuario.activo ? 'desactivar' : 'activar';
    if (!confirm(`¿Seguro que querés ${accion} a ${usuario.nombre} ${usuario.apellido}?`)) {
      return;
    }

    this.error.set(null);
    this.usuarioService.cambiarEstado(usuario.id, !usuario.activo).subscribe({
      next: actualizado => {
        this.usuarios.update(lista => lista.map(u => (u.id === actualizado.id ? actualizado : u)));
        this.mensaje.set(
          `${actualizado.nombre} ${actualizado.apellido} quedó ${actualizado.activo ? 'activo' : 'inactivo'}.`
        );
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  protected etiquetaRol(rol: Rol): string {
    return this.roles.find(r => r.valor === rol)?.etiqueta ?? rol;
  }

  protected invalido(campo: string): boolean {
    const control = this.form.get(campo);
    return !!control && control.touched && control.invalid;
  }
}