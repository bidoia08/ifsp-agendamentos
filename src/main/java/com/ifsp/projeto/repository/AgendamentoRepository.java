package com.ifsp.projeto.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ifsp.projeto.model.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
       @Query("""
    SELECT COUNT(a) > 0 FROM Agendamento a 
    WHERE a.sala = :sala 
    AND a.data = :data 
    AND (
        (a.horaInicio < :horaFim AND a.horaFim > :horaInicio)
    )
    AND (:id IS NULL OR a.id <> :id)
""")
boolean verificarConflitos(
    @Param("sala") String sala,
    @Param("data") LocalDate data,
    @Param("horaInicio") LocalTime horaInicio,
    @Param("horaFim") LocalTime horaFim,
    @Param("id") Long id
);

    List<Agendamento> findByData(LocalDate data);

}