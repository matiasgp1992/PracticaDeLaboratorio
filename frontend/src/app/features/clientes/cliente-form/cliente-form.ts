import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TipoCliente } from '../../../core/models/cliente.models';
import { ClienteService } from '../../../core/services/cliente.service';
import { mensajeDeError } from '../../../core/utils/errores';

@Component({
  selector: 'app-cliente-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-form.html'
})
export class ClienteForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly clienteService = inject(ClienteService);

  protected readonly idCliente = signal<number | null>(null);
  protected readonly esEdicion = computed(() => this.idCliente() !== null);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    tipo: ['PERSONA_FISICA' as TipoCliente, Validators.required],
    nombre: [''],
    apellido: [''],
    razonSocial: [''],
    documento: ['', [Validators.required, Validators.maxLength(13)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    telefono: ['', Validators.maxLength(30)],
    direccion: ['', Validators.maxLength(200)]
  });

  constructor() {
    this.form.controls.tipo.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe(tipo => this.ajustarValidadores(tipo));

    this.ajustarValidadores(this.form.controls.tipo.value);
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      return;
    }

    this.idCliente.set(Number(id));
    this.cargando.set(true);

    this.clienteService.obtener(Number(id)).subscribe({
      next: cliente => {
        this.form.patchValue({
          tipo: cliente.tipo,
          nombre: cliente.nombre ?? '',
          apellido: cliente.apellido ?? '',
          razonSocial: cliente.razonSocial ?? '',
          documento: cliente.documento,
          email: cliente.email ?? '',
          telefono: cliente.telefono ?? '',
          direccion: cliente.direccion ?? ''
        });
        this.cargando.set(false);
      },
      error: (e: HttpErrorResponse) => {
        this.error.set(mensajeDeError(e));
        this.cargando.set(false);
      }
    });
  }

  protected esFisica(): boolean {
    return this.form.controls.tipo.value === 'PERSONA_FISICA';
  }

  protected invalido(campo: string): boolean {
    const control = this.form.get(campo);
    return !!control && control.touched && control.invalid;
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    const datos = this.form.getRawValue();
    const id = this.idCliente();
    const pedido = id === null
      ? this.clienteService.crear(datos)
      : this.clienteService.actualizar(id, datos);

    pedido.subscribe({
      next: () => this.router.navigate(['/clientes']),
      error: (e: HttpErrorResponse) => {
        this.error.set(mensajeDeError(e));
        this.guardando.set(false);
      }
    });
  }

  private ajustarValidadores(tipo: TipoCliente): void {
    const { nombre, apellido, razonSocial } = this.form.controls;

    if (tipo === 'PERSONA_FISICA') {
      nombre.setValidators([Validators.required, Validators.maxLength(100)]);
      apellido.setValidators([Validators.required, Validators.maxLength(100)]);
      razonSocial.setValidators([Validators.maxLength(150)]);
    } else {
      nombre.setValidators([Validators.maxLength(100)]);
      apellido.setValidators([Validators.maxLength(100)]);
      razonSocial.setValidators([Validators.required, Validators.maxLength(150)]);
    }

    [nombre, apellido, razonSocial].forEach(control =>
      control.updateValueAndValidity({ emitEvent: false })
    );
  }
}