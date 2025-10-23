package com.ifsp.projeto.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sala;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String responsavel;
    private String emailContato;
    // ✅ Adicione este campo:
    @Column(columnDefinition = "TEXT")
    private String descricao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getEmailContato() { return emailContato; }
    public void setEmailContato(String emailContato) { this.emailContato = emailContato; }
    // ✅ Getters e setters novos
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}