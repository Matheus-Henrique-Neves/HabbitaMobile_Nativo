package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterUser extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail, edtSenha, edtConfirmarSenha;
    private TextView txtErro;
    private Button btnCadastrar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        auth = FirebaseAuth.getInstance();
        initViews();
        btnCadastrar.setOnClickListener(v -> realizarCadastro());
    }

    private void initViews() {
        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenhaCadastro);
        txtErro = findViewById(R.id.txtErroCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    private void realizarCadastro() {
        esconderErro();

        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String senha = edtSenha.getText().toString();
        String confirmar = edtConfirmarSenha.getText().toString();

        if (!validarCampos(nome, email, senha, confirmar)) return;

        btnCadastrar.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener(result -> {
                    if (result.getUser() != null) {
                        UserProfileChangeRequest perfil = new UserProfileChangeRequest.Builder()
                                .setDisplayName(nome)
                                .build();
                        result.getUser().updateProfile(perfil);
                    }
                    abrirTelaMenu();
                })
                .addOnFailureListener(e -> {
                    btnCadastrar.setEnabled(true);
                    String msg = e.getMessage() != null ? e.getMessage() : "Erro ao cadastrar";
                    if (msg.contains("email address is already in use")) {
                        mostrarErro("Este email ja esta cadastrado.");
                    } else if (msg.contains("badly formatted")) {
                        mostrarErro("Email invalido.");
                    } else {
                        mostrarErro(msg);
                    }
                });
    }

    private boolean validarCampos(String nome, String email, String senha, String confirmar) {
        if (nome.isEmpty()) { mostrarErro("Digite seu nome."); edtNome.requestFocus(); return false; }
        if (nome.length() < 2) { mostrarErro("Nome muito curto."); edtNome.requestFocus(); return false; }
        if (email.isEmpty()) { mostrarErro("Digite seu email."); edtEmail.requestFocus(); return false; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro("Email invalido."); edtEmail.requestFocus(); return false;
        }
        if (senha.isEmpty()) { mostrarErro("Digite sua senha."); edtSenha.requestFocus(); return false; }
        if (senha.length() < 6) { mostrarErro("Senha deve ter no minimo 6 caracteres."); edtSenha.requestFocus(); return false; }
        if (!senha.matches(".*[a-zA-Z].*") || !senha.matches(".*[0-9].*")) {
            mostrarErro("Senha deve conter letras e numeros."); edtSenha.requestFocus(); return false;
        }
        if (!senha.equals(confirmar)) { mostrarErro("As senhas nao coincidem."); edtConfirmarSenha.requestFocus(); return false; }
        return true;
    }

    private void abrirTelaMenu() {
        Intent intent = new Intent(RegisterUser.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarErro(String mensagem) {
        txtErro.setText(mensagem);
        txtErro.setVisibility(View.VISIBLE);
    }

    private void esconderErro() {
        txtErro.setVisibility(View.GONE);
    }
}
