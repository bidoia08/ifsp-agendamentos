package com.ifsp.projeto.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomeUsuario;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String tipo; // "USER" ou "ADMIN"

    @Column(nullable = false)
    private String palavraSeguranca;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getPalavraSeguranca() {return palavraSeguranca;}
    public void setPalavraSeguranca(String palavraSeguranca) {this.palavraSeguranca = palavraSeguranca;}
}