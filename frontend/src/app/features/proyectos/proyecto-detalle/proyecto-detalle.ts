import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Usuario } from '../../../core/models/auth.models';
import { EtapaProyecto, Miembro, ProyectoDetalle } from '../../../core/models/proyecto.models';
import { AuthService } from '../../../core/services/auth.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { ETAPAS, claseEstado, etiquetaEstado, etiquetaEtapa } from '../../../core/utils/etiquetas';

@Component({
  selector: 'app-proyecto-detalle',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './proyecto-detalle.html'
})
export class ProyectoDetallePagina implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly proyectoService = inject(ProyectoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly auth = inject(AuthService);

  protected readonly etapas = ETAPAS;
  protected readonly etiquetaEtapa = etiquetaEtapa;
  protected readonly etiquetaEstado = etiquetaEstado;
  protected readonly claseEstado = claseEstado;
  protected readonly puedeGestionar = this.auth.puedeGestionar;

  protected readonly proyecto = signal<ProyectoDetalle | null>(null);
  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly mensaje = signal<string | null>(null);

  protected readonly indiceEtapaActual = computed(() =>
    ETAPAS.findIndex(e => e.valor === this.proyecto()?.etapaActual)
  );

  /** Usuarios activos del estudio que todavía no son miembros */
  protected readonly usuariosDisponibles = computed(() => {
    const miembros = new Set(this.proyecto()?.miembros.map(m => m.usuarioId) ?? []);
    return this.usuarios().filter(u => u.activo && !miembros.has(u.id));
  });

  protected readonly etapaForm = this.fb.nonNullable.group({
    etapa: ['' as EtapaProyecto | '', Validators.required],
    observacion: ['', Validators.maxLength(500)]
  });

  protected readonly miembroForm = this.fb.nonNullable.group({
    usuarioId: [null as number | null, Validators.required],
    funcion: ['', [Validators.required, Validators.maxLength(100)]]
  });

  private id = 0;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    this.proyectoService.obtener(this.id).subscribe({
      next: proyecto => {
        this.proyecto.set(proyecto);
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      }
    });

    if (this.puedeGestionar()) {
      this.usuarioService.listar().subscribe(usuarios => this.usuarios.set(usuarios));
    }
  }

  protected cambiarEtapa(): void {
    if (this.etapaForm.invalid) {
      this.etapaForm.markAllAsTouched();
      return;
    }

    const { etapa, observacion } = this.etapaForm.getRawValue();
    this.limpiarMensajes();

    this.proyectoService.cambiarEtapa(this.id, { etapa: etapa as EtapaProyecto, observacion }).subscribe({
      next: proyecto => {
        this.proyecto.set(proyecto);
        this.etapaForm.reset();
        this.mensaje.set(`Etapa actualizada a ${etiquetaEtapa(proyecto.etapaActual)}.`);
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  protected agregarMiembro(): void {
    if (this.miembroForm.invalid) {
      this.miembroForm.markAllAsTouched();
      return;
    }

    const { usuarioId, funcion } = this.miembroForm.getRawValue();
    this.limpiarMensajes();

    this.proyectoService.agregarMiembro(this.id, { usuarioId: usuarioId as number, funcion }).subscribe({
      next: proyecto => {
        this.proyecto.set(proyecto);
        this.miembroForm.reset();
        this.mensaje.set('Miembro agregado al proyecto.');
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  protected quitarMiembro(miembro: Miembro): void {
    if (!confirm(`¿Quitar a ${miembro.nombre} ${miembro.apellido} del proyecto?`)) {
      return;
    }
    this.limpiarMensajes();

    this.proyectoService.quitarMiembro(this.id, miembro.usuarioId).subscribe({
      next: proyecto => {
        this.proyecto.set(proyecto);
        this.mensaje.set(`${miembro.nombre} ${miembro.apellido} fue quitado del proyecto.`);
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  private limpiarMensajes(): void {
    this.mensaje.set(null);
    this.error.set(null);
  }
}