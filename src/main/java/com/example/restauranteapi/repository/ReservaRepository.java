package com.example.restauranteapi.repository;

import com.example.restauranteapi.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByFecha(LocalDate fecha);
    List<Reserva> findByFechaAndMesa_Id(LocalDate fecha, Long mesaId);
    List<Reserva> findByUsuario_Id(Long usuarioId);
    List<Reserva> findByFechaAndHora(LocalDate fecha, String hora);}
