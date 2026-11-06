import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { Pagina } from '../../../core/models/pagina.model';
import { EstadoProyecto, EtapaProyecto, Proyecto } from '../../../core/models/proyecto.models';
import { AuthService } from '../../../core/services/auth.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { ProyectoService } from '../../../core/services/proyecto.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { ESTADOS, ETAPAS, claseEstado, etiquetaEstado, etiquetaEtapa } from '../../../core/utils/etiquetas';

@Component({
  selector: 'app-proyecto-lista',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './proyecto-lista.html'
})
export class ProyectoLista {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly proyectoService = inject(ProyectoService);
  private readonly clienteService = inject(ClienteService);
  private readonly auth = inject(AuthService);

  protected readonly etapas = ETAPAS;
  protected readonly estados = ESTADOS;
  protected readonly etiquetaEtapa = etiquetaEtapa;
  protected readonly etiquetaEstado = etiquetaEstado;
  protected readonly claseEstado = claseEstado;

  protected readonly puedeGestionar = this.auth.puedeGestionar;
  protected readonly resultado = signal<Pagina<Proyecto> | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly clienteFiltro = signal<{ id: number; denominacion: string } | null>(null);

  protected readonly paginas = computed(() =>
    Array.from({ length: this.resultado()?.totalPaginas ?? 0 }, (_, i) => i)
  );

  protected readonly filtros = this.fb.nonNullable.group({
    texto: [''],
    etapa: ['' as EtapaProyecto | ''],
    estado: ['' as EstadoProyecto | '']
  });

  private paginaActual = 0;
  private readonly tamanio = 10;

  constructor() {
    // Escucha ?clienteId=X en la URL (historial de proyectos de un cliente)
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe(params => {
      const clienteId = params.get('clienteId');
      this.paginaActual = 0;

      if (clienteId) {
        this.clienteFiltro.set({ id: Number(clienteId), denominacion: '' });
        this.clienteService.obtener(Number(clienteId)).subscribe({
          next: c => this.clienteFiltro.set({ id: c.id, denominacion: c.denominacion }),
          error: () => this.clienteFiltro.set({ id: Number(clienteId), denominacion: 'Cliente' })
        });
      } else {
        this.clienteFiltro.set(null);
      }
      this.cargar();
    });

    this.filtros.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed())
      .subscribe(() => {
        this.paginaActual = 0;
        this.cargar();
      });
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.proyectoService
      .buscar({
        ...this.filtros.getRawValue(),
        clienteId: this.clienteFiltro()?.id ?? null,
        pagina: this.paginaActual,
        tamanio: this.tamanio
      })
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
}