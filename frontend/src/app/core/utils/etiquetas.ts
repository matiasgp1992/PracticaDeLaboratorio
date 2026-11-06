import { EstadoProyecto, EtapaProyecto } from '../models/proyecto.models';

export const ETAPAS: { valor: EtapaProyecto; etiqueta: string }[] = [
  { valor: 'ANTEPROYECTO', etiqueta: 'Anteproyecto' },
  { valor: 'PROYECTO_EJECUTIVO', etiqueta: 'Proyecto Ejecutivo' },
  { valor: 'DIRECCION_DE_OBRA', etiqueta: 'Dirección de Obra' }
];

export const ESTADOS: { valor: EstadoProyecto; etiqueta: string }[] = [
  { valor: 'EN_CURSO', etiqueta: 'En curso' },
  { valor: 'PAUSADO', etiqueta: 'Pausado' },
  { valor: 'FINALIZADO', etiqueta: 'Finalizado' },
  { valor: 'CANCELADO', etiqueta: 'Cancelado' }
];

export function etiquetaEtapa(etapa: EtapaProyecto): string {
  return ETAPAS.find(e => e.valor === etapa)?.etiqueta ?? etapa;
}

export function etiquetaEstado(estado: EstadoProyecto): string {
  return ESTADOS.find(e => e.valor === estado)?.etiqueta ?? estado;
}

export function claseEstado(estado: EstadoProyecto): string {
  switch (estado) {
    case 'EN_CURSO': return 'bg-success';
    case 'PAUSADO': return 'bg-warning text-dark';
    case 'FINALIZADO': return 'bg-primary';
    default: return 'bg-secondary';
  }
}