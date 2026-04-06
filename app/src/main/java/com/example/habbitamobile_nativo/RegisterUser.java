package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habbitamobile_nativo.dao.UsuarioDAO;
import com.example.habbitamobile_nativo.model.Usuario;

public class RegisterUser extends AppCompatActivity {

    private EditText txtEmail, txtSenha;
    private Button btnCadastrar;
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        txtEmail = findViewById(R.id.edtEmailCadastro);
        txtSenha = findViewById(R.id.edtSenhaCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        usuarioDAO = new UsuarioDAO(this);

        btnCadastrar.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String senha = txtSenha.getText().toString();

            if (!email.isEmpty() && !senha.isEmpty()) {
                Usuario usuario = new Usuario(email, senha);
                boolean sucesso = usuarioDAO.inserir(usuario);

                if (sucesso) {
                    Toast.makeText(this, "Cadastro Realizado",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Usuário já existe", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Preench todos os campos", Toast.LENGTH_SHORT).show();

            }
        });


    }
}