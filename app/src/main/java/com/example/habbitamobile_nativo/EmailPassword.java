package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPassword extends AppCompatActivity {

    private TextInputEditText edtSenhaAtual, edtNovaSenha, edtConfirmarSenha;
    private TextView txtMensagem;
    private Button btnAlterarSenha;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        TextView txtEmail = findViewById(R.id.txtEmail);
        edtSenhaAtual = findViewById(R.id.edtSenhaAtual);
        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);
        txtMensagem = findViewById(R.id.txtMensagem);
        btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        btnVoltar.setOnClickListener(v -> finish());
        btnAlterarSenha.setOnClickListener(v -> alterarSenha());
    }

    private void alterarSenha() {
        String senhaAtual = edtSenhaAtual.getText() != null ? edtSenhaAtual.getText().toString() : "";
        String novaSenha = edtNovaSenha.getText() != null ? edtNovaSenha.getText().toString() : "";
        String confirmar = edtConfirmarSenha.getText() != null ? edtConfirmarSenha.getText().toString() : "";

        if (senhaAtual.isEmpty()) { mostrarMensagem("Digite a senha atual.", true); return; }
        if (novaSenha.length() < 6) { mostrarMensagem("A nova senha deve ter no minimo 6 caracteres.", true); return; }
        if (!novaSenha.matches(".*[a-zA-Z].*") || !novaSenha.matches(".*[0-9].*")) {
            mostrarMensagem("A nova senha deve conter letras e numeros.", true); return;
        }
        if (!novaSenha.equals(confirmar)) { mostrarMensagem("As senhas nao coincidem.", true); return; }

        if (user.getEmail() == null) { mostrarMensagem("Conta sem email valido.", true); return; }

        btnAlterarSenha.setEnabled(false);
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), senhaAtual);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> user.updatePassword(novaSenha)
                        .addOnSuccessListener(u -> {
                            btnAlterarSenha.setEnabled(true);
                            limparCampos();
                            mostrarMensagem("Senha alterada com sucesso.", false);
                        })
                        .addOnFailureListener(e -> {
                            btnAlterarSenha.setEnabled(true);
                            mostrarMensagem("Erro ao alterar senha: " + e.getMessage(), true);
                        }))
                .addOnFailureListener(e -> {
                    btnAlterarSenha.setEnabled(true);
                    mostrarMensagem("Senha atual incorreta.", true);
                });
    }

    private void limparCampos() {
        edtSenhaAtual.setText("");
        edtNovaSenha.setText("");
        edtConfirmarSenha.setText("");
    }

    private void mostrarMensagem(String texto, boolean erro) {
        txtMensagem.setText(texto);
        txtMensagem.setTextColor(erro ? 0xFFB00020 : 0xFF2E7D32);
        txtMensagem.setVisibility(View.VISIBLE);
    }
}
