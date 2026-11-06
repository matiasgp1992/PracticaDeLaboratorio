import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { TipoCliente } from '../../../core/models/cliente.models';
import { EstadoProyecto, ProyectoRequest } from '../../../core/models/proyecto.models';
import { ClienteService } from '../../../core/services/cliente.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { ESTADOS } from '../../../core/utils/etiquetas';

interface OpcionCliente {
  id: number;
  denominacion: string;
}

@Component({
  selector: 'app-proyecto-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './proyecto-form.html'
})
export class ProyectoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly proyectoService = inject(ProyectoService);
  private readonly clienteService = inject(ClienteService);

  protected readonly estados = ESTADOS;
  protected readonly idProyecto = signal<number | null>(null);
  protected readonly esEdicion = computed(() => this.idProyecto() !== null);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly clientes = signal<OpcionCliente[]>([]);
  protected readonly modoCliente = signal<'existente' | 'nuevo'>('existente');

  protected readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: ['', Validators.maxLength(1000)],
    direccionObra: ['', Validators.maxLength(200)],
    clienteId: [null as number | null, Validators.required],
    fechaInicio: [''],
    fechaFinEstimada: [''],
    estado: ['EN_CURSO' as EstadoProyecto]
  });

  protected readonly nuevoCliente = this.fb.nonNullable.group({
    tipo: ['PERSONA_FISICA' as TipoCliente],
    nombre: [''],
    apellido: [''],
    razonSocial: [''],
    documento: ['', [Validators.required, Validators.maxLength(13)]],
    email: ['', Validators.email],
    telefono: ['', Validators.maxLength(30)]
  });

  constructor() {
    this.nuevoCliente.controls.tipo.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(tipo => this.ajustarValidadoresCliente(tipo));
    this.ajustarValidadoresCliente('PERSONA_FISICA');
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    const clientes$ = this.clienteService.buscar({
      texto: '', tipo: '', incluirInactivos: false, pagina: 0, tamanio: 100
    });

    if (!id) {
      clientes$.subscribe({
        next: r => {
          this.clientes.set(r.contenido.map(c => ({ id: c.id, denominacion: c.denominacion })));
          this.cargando.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(mensajeDeError(e));
          this.cargando.set(false);
        }
      });
      return;
    }

    this.idProyecto.set(Number(id));

    forkJoin({ clientes: clientes$, proyecto: this.proyectoService.obtener(Number(id)) }).subscribe({
      next: ({ clientes, proyecto }) => {
        const opciones = clientes.contenido.map(c => ({ id: c.id, denominacion: c.denominacion }));
        // Si el cliente actual del proyecto está dado de baja, igual lo mostramos
        if (!opciones.some(o => o.id === proyecto.clienteId)) {
          opciones.push({ id: proyecto.clienteId, denominacion: `${proyecto.clienteDenominacion} (de baja)` });
        }
        this.clientes.set(opciones);

        this.form.patchValue({
          nombre: proyecto.nombre,
          descripcion: proyecto.descripcion ?? '',
          direccionObra: proyecto.direccionObra ?? '',
          clienteId: proyecto.clienteId,
          fechaInicio: proyecto.fechaInicio ?? '',
          fechaFinEstimada: proyecto.fechaFinEstimada ?? '',
          estado: proyecto.estado
        });
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      }
    });
  }

  protected cambiarModoCliente(modo: 'existente' | 'nuevo'): void {
    this.modoCliente.set(modo);
    const control = this.form.controls.clienteId;
    control.setValidators(modo === 'existente' ? Validators.required : null);
    control.updateValueAndValidity();
  }

  protected esFisica(): boolean {
    return this.nuevoCliente.controls.tipo.value === 'PERSONA_FISICA';
  }

  protected invalido(campo: string): boolean {
    const control = this.form.get(campo);
    return !!control && control.touched && control.invalid;
  }

  protected invalidoCliente(campo: string): boolean {
    const control = this.nuevoCliente.get(campo);
    return !!control && control.touched && control.invalid;
  }

  protected guardar(): void {
    const conClienteNuevo = this.modoCliente() === 'nuevo';

    if (this.form.invalid || (conClienteNuevo && this.nuevoCliente.invalid)) {
      this.form.markAllAsTouched();
      if (conClienteNuevo) this.nuevoCliente.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    // 1) Si es cliente nuevo, primero lo creamos; si no, usamos el elegido
    const clienteId$: Observable<number> = conClienteNuevo
      ? this.clienteService.crear({ ...this.nuevoCliente.getRawValue(), direccion: '' }).pipe(
          tap(cliente => {
            // Si después falla el proyecto, el cliente ya existe: lo dejamos seleccionado
            this.clientes.update(lista => [...lista, { id: cliente.id, denominacion: cliente.denominacion }]);
            this.form.controls.clienteId.setValue(cliente.id);
            this.cambiarModoCliente('existente');
          }),
          map(cliente => cliente.id)
        )
      : of(this.form.controls.clienteId.value as number);

    // 2) Con el id del cliente, creamos o actualizamos el proyecto
    clienteId$
      .pipe(
        switchMap(clienteId => {
          const valores = this.form.getRawValue();
          const datos: ProyectoRequest = {
            nombre: valores.nombre,
            descripcion: valores.descripcion,
            direccionObra: valores.direccionObra,
            clienteId,
            fechaInicio: valores.fechaInicio || null,
            fechaFinEstimada: valores.fechaFinEstimada || null,
            estado: this.esEdicion() ? valores.estado : null
          };
          const id = this.idProyecto();
          return id === null
            ? this.proyectoService.crear(datos)
            : this.proyectoService.actualizar(id, datos);
        })
      )
      .subscribe({
        next: proyecto => this.router.navigate(['/proyectos', proyecto.id]),
        error: (e: HttpErrorResponse) => {
          this.error.set(mensajeDeError(e));
          this.guardando.set(false);
        }
      });
  }

  private ajustarValidadoresCliente(tipo: TipoCliente): void {
    const { nombre, apellido, razonSocial } = this.nuevoCliente.controls;
    const fisica = tipo === 'PERSONA_FISICA';

    nombre.setValidators(fisica ? [Validators.required, Validators.maxLength(100)] : null);
    apellido.setValidators(fisica ? [Validators.required, Validators.maxLength(100)] : null);
    razonSocial.setValidators(fisica ? null : [Validators.required, Validators.maxLength(150)]);

    [nombre, apellido, razonSocial].forEach(c => c.updateValueAndValidity({ emitEvent: false }));
  }
}