package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class PersonalInfo extends AppCompatActivity {

    private TextInputEditText edtNome, edtEmail;
    private TextView txtMensagem;
    private Button btnSalvar;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        txtMensagem = findViewById(R.id.txtMensagem);
        btnSalvar = findViewById(R.id.btnSalvar);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        edtNome.setText(user.getDisplayName() != null ? user.getDisplayName() : "");
        edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        btnVoltar.setOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void salvar() {
        String nome = edtNome.getText() != null ? edtNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            mostrarMensagem("Digite seu nome.", true);
            return;
        }

        btnSalvar.setEnabled(false);
        UserProfileChangeRequest perfil = new UserProfileChangeRequest.Builder()
                .setDisplayName(nome)
                .build();

        user.updateProfile(perfil)
                .addOnSuccessListener(unused -> {
                    btnSalvar.setEnabled(true);
                    mostrarMensagem("Dados atualizados com sucesso.", false);
                })
                .addOnFailureListener(e -> {
                    btnSalvar.setEnabled(true);
                    mostrarMensagem("Erro ao salvar: " + e.getMessage(), true);
                });
    }

    private void mostrarMensagem(String texto, boolean erro) {
        txtMensagem.setText(texto);
        txtMensagem.setTextColor(erro ? 0xFFB00020 : 0xFF2E7D32);
        txtMensagem.setVisibility(View.VISIBLE);
    }
}
