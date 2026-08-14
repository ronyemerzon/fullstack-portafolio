import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SistemasService } from './sistemas.service';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

/**
 * Componente Angular Standalone sanitizado para la Gestión de Activos TI.
 * Demuestra manejo de Formularios Reactivos, suscripción RxJS a valueChanges y exportación PDF.
 */
@Component({
  selector: 'app-sistemas-activos',
  standalone: true,
  templateUrl: './activos.html'
})
export class SistemasActivos implements OnInit {
  private fb = inject(FormBuilder);
  private sistemasService = inject(SistemasService);

  activeTab: 'personal' | 'catalogo' = 'personal';
  empleadosActivos: any[] = [];
  catalogoActivos: any[] = [];
  formActivo!: FormGroup;

  ngOnInit() {
    this.cargarCatalogoActivos();
    this.initFormActivo();
  }

  initFormActivo() {
    this.formActivo = this.fb.group({
      codigoPatrimonial: [''],
      tipoActivo: ['LAPTOP', Validators.required],
      marca: ['', Validators.required],
      modelo: ['', Validators.required],
      numeroSerie: ['', Validators.required],
      estado: ['DISPONIBLE', Validators.required]
    });

    // Auto-generador dinámico de código patrimonial al cambiar selección
    this.formActivo.get('tipoActivo')?.valueChanges.subscribe(tipo => {
      if (tipo) {
        const nextCode = this.generarSiguienteCodigo(tipo);
        this.formActivo.patchValue({ codigoPatrimonial: nextCode }, { emitEvent: false });
      }
    });
  }

  cargarCatalogoActivos() {
    this.sistemasService.getActivosTi().subscribe({
      next: (res) => this.catalogoActivos = res,
      error: (err) => console.error('Error al cargar catálogo:', err)
    });
  }

  generarSiguienteCodigo(tipo: string): string {
    let prefix = 'ICO-ACT-';
    if (tipo === 'LAPTOP') prefix = 'ICO-LAP-';
    else if (tipo === 'CELULAR') prefix = 'ICO-CEL-';
    else if (tipo === 'MONITOR') prefix = 'ICO-MON-';
    
    let maxNum = 0;
    this.catalogoActivos.forEach(a => {
      const code = a.codigoPatrimonial || '';
      if (code.startsWith(prefix)) {
        const num = parseInt(code.substring(prefix.length), 10);
        if (!isNaN(num) && num > maxNum) maxNum = num;
      }
    });
    return `${prefix}${String(maxNum + 1).padStart(4, '0')}`;
  }
}
