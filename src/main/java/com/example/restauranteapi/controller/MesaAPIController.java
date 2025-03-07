package com.example.restauranteapi.controller;

import com.example.restauranteapi.entity.Mesa;
import com.example.restauranteapi.entity.Reserva;
import com.example.restauranteapi.repository.MesaRepository;
import com.example.restauranteapi.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MesaAPIController {
    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping("/mesas")
    public ResponseEntity getListMesas() {
        List mesas = mesaRepository.findAll();
        return ResponseEntity.ok(mesas);
    }

    @PostMapping("/mesas")
    public ResponseEntity insertMesa(@RequestBody Mesa mesa) {
        Mesa mesaGuardada = (Mesa) mesaRepository.save(mesa);
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaGuardada);
    }

    @PutMapping("/mesas/{id}")
    public ResponseEntity updateMesa(@PathVariable Long id, @RequestBody Mesa mesaEditada) {
        Mesa mesaExistente = (Mesa) mesaRepository.findById(id).orElse(null);
        if (mesaExistente == null) {
            return ResponseEntity.notFound().build();
        }
        if (mesaEditada.getDescripcion() != null) {
            mesaExistente.setDescripcion(mesaEditada.getDescripcion());
        }
        if (mesaEditada.getNMesa() != null) {
            mesaExistente.setNMesa(mesaEditada.getNMesa());
        }
        Mesa mesaActualizada = (Mesa) mesaRepository.save(mesaExistente);
        return ResponseEntity.ok(mesaActualizada);
    }

    @DeleteMapping("/mesas/{id}")
    public ResponseEntity deleteMesa(@PathVariable Long id) {
        Mesa mesa = (Mesa) mesaRepository.findById(id).orElse(null);
        if (mesa == null) {
            return ResponseEntity.notFound().build();
        }
        mesaRepository.delete(mesa);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mesas/disponibles")
    public ResponseEntity getMesasDisponibles(
            @RequestParam("fecha") String fecha, // "YYYY-MM-DD"
            @RequestParam("hora") String hora    // "HH:MM"
    ) {
        LocalDate fechaReserva = LocalDate.parse(fecha);

        List todasLasMesas = mesaRepository.findAll();

        List reservas = reservaRepository.findByFechaAndHora(fechaReserva, hora);

        List mesasDisponibles = new ArrayList(todasLasMesas);

        // Quitar las mesas que ya están reservadas
        for (int i = 0; i < reservas.size(); i++) {
            Reserva reserva = (Reserva) reservas.get(i);
            Long mesaReservadaId = reserva.getMesa().getId();

            for (int j = 0; j < mesasDisponibles.size(); j++) {
                Mesa mesa = (Mesa) mesasDisponibles.get(j);
                if (mesa.getId().equals(mesaReservadaId)) {
                    mesasDisponibles.remove(j);
                    break;
                }
            }
        }

        return ResponseEntity.ok(mesasDisponibles);
    }
}