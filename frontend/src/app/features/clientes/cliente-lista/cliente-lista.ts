import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { Cliente, TipoCliente } from '../../../core/models/cliente.models';
import { Pagina } from '../../../core/models/pagina.model';
import { AuthService } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { mensajeDeError } from '../../../core/utils/errores';

@Component({
  selector: 'app-cliente-lista',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-lista.html'
})
export class ClienteLista {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly auth = inject(AuthService);

  protected readonly puedeGestionar = this.auth.puedeGestionar;
  protected readonly resultado = signal<Pagina<Cliente> | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly mensaje = signal<string | null>(null);

  protected readonly paginas = computed(() =>
    Array.from({ length: this.resultado()?.totalPaginas ?? 0 }, (_, i) => i)
  );

  protected readonly filtros = this.fb.nonNullable.group({
    texto: [''],
    tipo: ['' as TipoCliente | ''],
    incluirInactivos: [false]
  });

  private paginaActual = 0;
  private readonly tamanio = 10;

  constructor() {
    this.filtros.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed())
      .subscribe(() => {
        this.paginaActual = 0;
        this.cargar();
      });

    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.clienteService
      .buscar({ ...this.filtros.getRawValue(), pagina: this.paginaActual, tamanio: this.tamanio })
      .subscribe({
        next: resultado => {
          this.resultado.set(resultado);
          this.cargando.set(false);
        },
        error: (e: HttpErrorResponse) => {
          this.error.set(mensajeDeError(e));
          this.cargando.set(false);
        }
      });
  }

  protected irAPagina(numero: number): void {
    const total = this.resultado()?.totalPaginas ?? 0;
    if (numero < 0 || numero >= total || numero === this.paginaActual) {
      return;
    }
    this.paginaActual = numero;
    this.cargar();
  }

  protected darDeBaja(cliente: Cliente): void {
    if (!confirm(`¿Seguro que querés dar de baja a ${cliente.denominacion}?`)) {
      return;
    }
    this.clienteService.darDeBaja(cliente.id).subscribe({
      next: () => {
        this.mensaje.set(`${cliente.denominacion} fue dado de baja.`);
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  protected reactivar(cliente: Cliente): void {
    this.clienteService.reactivar(cliente.id).subscribe({
      next: () => {
        this.mensaje.set(`${cliente.denominacion} fue reactivado.`);
        this.cargar();
      },
      error: (e: HttpErrorResponse) => this.error.set(mensajeDeError(e))
    });
  }

  protected etiquetaTipo(tipo: TipoCliente): string {
    return tipo === 'PERSONA_FISICA' ? 'Persona física' : 'Persona jurídica';
  }
}