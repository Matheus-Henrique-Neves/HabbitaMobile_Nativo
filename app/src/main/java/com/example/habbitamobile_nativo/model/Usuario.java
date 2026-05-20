package com.example.habbitamobile_nativo.model;

public class Usuario {
    private int id;
    private String email;
    private String senha;

    // Construtores
    public Usuario() {
        // Construtor vazio
    }

    public Usuario(String email, String senha) {
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.senha = senha;
    }

    public Usuario(int id, String email, String senha) {
        this.id = id;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.senha = senha;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    // Método auxiliar para validação
    public boolean isValid() {
        return email != null && !email.isEmpty() &&
                senha != null && !senha.isEmpty();
    }
}