package com.example.restauranteapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class ReservasConClientesDTO {
    // Cliente
    private String nombre;
    private String email;
    private String telefono;
    // Mesa
    private Long numeroMesa;
    private String descripcionMesa;
    // Reserva
    private Date fechaReserva;
    private String hora;
    private Integer nPersonas;
}
