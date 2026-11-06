import { Rol } from './auth.models';

export type EtapaProyecto = 'ANTEPROYECTO' | 'PROYECTO_EJECUTIVO' | 'DIRECCION_DE_OBRA';
export type EstadoProyecto = 'EN_CURSO' | 'PAUSADO' | 'FINALIZADO' | 'CANCELADO';

export interface Proyecto {
  id: number;
  nombre: string;
  direccionObra: string | null;
  clienteId: number;
  clienteDenominacion: string;
  etapaActual: EtapaProyecto;
  estado: EstadoProyecto;
  fechaInicio: string | null;
  fechaFinEstimada: string | null;
  fechaAlta: string;
}

export interface Miembro {
  usuarioId: number;
  nombre: string;
  apellido: string;
  email: string;
  rol: Rol;
  funcion: string;
  fechaAsignacion: string;
}

export interface HistorialEtapa {
  id: number;
  etapaAnterior: EtapaProyecto | null;
  etapaNueva: EtapaProyecto;
  usuarioNombre: string;
  observacion: string | null;
  fecha: string;
}

export interface ProyectoDetalle {
  id: number;
  nombre: string;
  descripcion: string | null;
  direccionObra: string | null;
  clienteId: number;
  clienteDenominacion: string;
  clienteActivo: boolean;
  etapaActual: EtapaProyecto;
  estado: EstadoProyecto;
  fechaInicio: string | null;
  fechaFinEstimada: string | null;
  fechaAlta: string;
  miembros: Miembro[];
  historial: HistorialEtapa[];
}

export interface ProyectoRequest {
  nombre: string;
  descripcion: string;
  direccionObra: string;
  clienteId: number;
  fechaInicio: string | null;
  fechaFinEstimada: string | null;
  estado: EstadoProyecto | null;
}

export interface CambiarEtapaRequest {
  etapa: EtapaProyecto;
  observacion: string;
}

export interface AgregarMiembroRequest {
  usuarioId: number;
  funcion: string;
}

export interface FiltrosProyecto {
  texto: string;
  clienteId: number | null;
  etapa: EtapaProyecto | '';
  estado: EstadoProyecto | '';
  pagina: number;
  tamanio: number;
}