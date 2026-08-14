package com.ico.erpico.modules.auth.controller;

import com.ico.erpico.modules.auth.entity.ActivoTi;
import com.ico.erpico.modules.auth.entity.CargoAsignacion;
import com.ico.erpico.modules.auth.repository.ActivoTiRepository;
import com.ico.erpico.modules.auth.repository.CargoAsignacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller sanitizado para la gestión del módulo de Sistemas TI y Control de Activos.
 * Demuestra arquitectura en capas, control transaccional y carga de actas/cargos firmados en PDF.
 */
@RestController
@RequestMapping("/api/sistemas")
public class SistemasController {

    @Autowired
    private ActivoTiRepository activoTiRepository;

    @Autowired
    private CargoAsignacionRepository cargoAsignacionRepository;

    // --- LISTAR ACTIVOS EN CATÁLOGO ---
    @GetMapping("/activos-ti")
    public ResponseEntity<?> getActivosTi() {
        try {
            return ResponseEntity.ok(activoTiRepository.findAll());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al listar activos", "error", ex.getMessage()));
        }
    }

    // --- REGISTRAR ACTIVO EN CATÁLOGO CON CÓDIGO AUTOGENERADO ---
    @PostMapping("/activos-ti")
    @Transactional
    public ResponseEntity<?> crearActivoTi(@RequestBody ActivoTi activo) {
        try {
            if (activo.getEstado() == null) {
                activo.setEstado("DISPONIBLE");
            }
            // Auto-generación de Código Patrimonial según tipo de activo (ej. ICO-LAP-0001, ICO-CEL-0001)
            if (activo.getCodigoPatrimonial() == null || activo.getCodigoPatrimonial().trim().isEmpty()) {
                String prefix = "ICO-ACT-";
                String tipo = activo.getTipoActivo() != null ? activo.getTipoActivo().toUpperCase() : "";
                if ("LAPTOP".equals(tipo)) prefix = "ICO-LAP-";
                else if ("CELULAR".equals(tipo)) prefix = "ICO-CEL-";
                else if ("MONITOR".equals(tipo)) prefix = "ICO-MON-";
                else if ("TECLADO".equals(tipo)) prefix = "ICO-TEC-";
                else if ("MOUSE".equals(tipo)) prefix = "ICO-MOU-";
                else if ("TABLET".equals(tipo)) prefix = "ICO-TAB-";
                else if ("CARGADOR".equals(tipo)) prefix = "ICO-CAR-";

                long count = activoTiRepository.count() + 1;
                String generated = prefix + String.format("%04d", count);
                while (activoTiRepository.findByCodigoPatrimonial(generated).isPresent()) {
                    count++;
                    generated = prefix + String.format("%04d", count);
                }
                activo.setCodigoPatrimonial(generated);
            }
            ActivoTi guardado = activoTiRepository.save(activo);
            return ResponseEntity.ok(guardado);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al registrar activo", "error", ex.getMessage()));
        }
    }

    // --- CARGAR ACTA FIRMADA EN PDF (MULTIPART UPLOAD) ---
    @PostMapping("/activos-ti/asignaciones/{id}/upload")
    @Transactional
    public ResponseEntity<?> subirCargoPdf(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<CargoAsignacion> opt = cargoAsignacionRepository.findById(id);
            if (opt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Asignación no encontrada");
            }
            CargoAsignacion asignacion = opt.get();
            
            // Guardar archivo sanitizado en servidor
            String uploadDir = "./uploads/cargos/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "cargo_firmado_" + id + "_" + System.currentTimeMillis() + ".pdf";
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            asignacion.setRutaPdfFirmado(fileName);
            cargoAsignacionRepository.save(asignacion);

            return ResponseEntity.ok(Map.of("mensaje", "Cargo PDF subido con éxito", "archivo", fileName));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("mensaje", "Error al guardar el archivo", "error", ex.getMessage()));
        }
    }
}
