package com.example.restauranteapi.controller;

import com.example.restauranteapi.entity.Reserva;
import com.example.restauranteapi.entity.Usuario;
import com.example.restauranteapi.repository.ReservaRepository;
import com.example.restauranteapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Importar LocalDate
import java.util.List;

@RestController
public class ReservaAPIController {
    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    public int convertirHorasStringAMinutos(String hora) {
        System.out.println("Convirtiendo hora: " + hora);
        String[] partesHora = hora.split(":");
        int horas = Integer.parseInt(partesHora[0]);
        int minutos = Integer.parseInt(partesHora[1]);
        return horas * 60 + minutos;
    }

    public boolean sePuedeReservar(LocalDate fecha, String hora, Long idMesa) {
        System.out.println("Verificando reserva - Fecha: " + fecha + ", Hora: " + hora + ", Mesa ID: " + idMesa);
        List<Reserva> reservas = reservaRepository.findByFechaAndMesa_Id(fecha, idMesa);
        System.out.println("Reservas encontradas: " + reservas.size());
        int minutosReserva = convertirHorasStringAMinutos(hora);

        for (Reserva reserva : reservas) {
            int minutosExistenteTotal = convertirHorasStringAMinutos(reserva.getHora());
            if (minutosReserva >= (minutosExistenteTotal - 59) && minutosReserva <= (minutosExistenteTotal + 59)) {
                System.out.println("Conflicto encontrado");
                return false;
            }
        }
        return true;
    }

    /*

    {
        "fecha": "2025-03-30",
        "hora": "18:00",
        "nPersonas": 4,
        "usuario": {
            "id": 1
        },
        "mesa": {
            "id": 4
        }
    }

     */

    @PostMapping("/reservas")
    public ResponseEntity<Reserva> insertReserva(@RequestBody Reserva reserva, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Asociar el usuario a la reserva
        reserva.setUsuario(usuario);

        if (sePuedeReservar(reserva.getFecha(), reserva.getHora(), reserva.getMesa().getId())) {
            var reservaGuardada = reservaRepository.save(reserva);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservaGuardada);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<?> deleteReserva(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return reservaRepository.findById(id)
                .map(reserva -> {
                    // Verificar que la reserva pertenece al usuario autenticado
                    if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    reservaRepository.delete(reserva);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/reservas")
    public ResponseEntity<List<Reserva>> getReservasByUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName(); // Obtiene el email/username
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Reserva> reservas = reservaRepository.findByUsuario_Id(usuario.getId());

        return ResponseEntity.ok(reservas);
    }
}