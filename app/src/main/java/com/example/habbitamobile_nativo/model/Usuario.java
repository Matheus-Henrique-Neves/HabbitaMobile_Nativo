package com.example.habbitamobile_nativo.model;

public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;

    public Usuario() {}

    public Usuario(String nome, String email, String senha) {
        this.nome = nome != null ? nome.trim() : null;
        this.email = email != null ? email.trim().toLowerCase() : null;
        this.senha = senha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome != null ? nome.trim() : null; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : null; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isValid() {
        return nome != null && !nome.isEmpty() &&
                email != null && !email.isEmpty() &&
                senha != null && !senha.isEmpty();
    }
}
