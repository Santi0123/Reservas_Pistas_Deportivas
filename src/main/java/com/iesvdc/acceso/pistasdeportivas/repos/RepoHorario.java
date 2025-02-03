package com.iesvdc.acceso.pistasdeportivas.repos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iesvdc.acceso.pistasdeportivas.modelos.Horario;
import com.iesvdc.acceso.pistasdeportivas.modelos.Instalacion;

@Repository
public interface RepoHorario extends JpaRepository<Horario, Long> {

    Page<Horario> findByInstalacion(Instalacion instalacion, Pageable pageable);

    List<Horario> findByInstalacion(Instalacion instalacion);

    @Query("""
            SELECT h 
            FROM Horario h
            WHERE h.instalacion = :instalacion
            AND h.id NOT IN (
                SELECT r.horario.id 
                FROM Reserva r 
                WHERE r.fecha = :fecha
                )
            """)
    List<Horario> findHorariosDisponibles(@Param("instalacion") Instalacion instalacion,
            @Param("fecha") LocalDate fecha);

}
