export type TipoCliente = 'PERSONA_FISICA' | 'PERSONA_JURIDICA';

export interface Cliente {
  id: number;
  tipo: TipoCliente;
  nombre: string | null;
  apellido: string | null;
  razonSocial: string | null;
  denominacion: string;
  documento: string;
  email: string | null;
  telefono: string | null;
  direccion: string | null;
  activo: boolean;
  fechaAlta: string;
}

export interface ClienteRequest {
  tipo: TipoCliente;
  nombre: string;
  apellido: string;
  razonSocial: string;
  documento: string;
  email: string;
  telefono: string;
  direccion: string;
}

export interface FiltrosCliente {
  texto: string;
  tipo: TipoCliente | '';
  incluirInactivos: boolean;
  pagina: number;
  tamanio: number;
}