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

        initViews();
        initObjects();
        setupListeners();
    }

    private void initViews() {
        txtEmail = findViewById(R.id.edtEmailCadastro);
        txtSenha = findViewById(R.id.edtSenhaCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    private void initObjects() {
        usuarioDAO = new UsuarioDAO(this);
    }

    private void setupListeners() {
        btnCadastrar.setOnClickListener(v -> realizarCadastro());
    }

    private void realizarCadastro() {
        String email = txtEmail.getText().toString().trim().toLowerCase();
        String senha = txtSenha.getText().toString();

        if (!validarCampos(email, senha)) {
            return;
        }

        // Verificar se email já existe
        if (usuarioDAO.emailExiste(email)) {
            txtEmail.setError("Este email já está cadastrado");
            txtEmail.requestFocus();
            Toast.makeText(this, "Email já cadastrado", Toast.LENGTH_LONG).show();
            return;
        }

        Usuario usuario = new Usuario(email, senha);
        boolean sucesso = usuarioDAO.inserir(usuario);

        if (sucesso) {
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validarCampos(String email, String senha) {
        if (email.isEmpty()) {
            txtEmail.setError("Digite seu email");
            txtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Digite um email válido");
            txtEmail.requestFocus();
            return false;
        }

        if (senha.isEmpty()) {
            txtSenha.setError("Digite sua senha");
            txtSenha.requestFocus();
            return false;
        }

        if (senha.length() < 4) {
            txtSenha.setError("Senha deve ter no mínimo 4 caracteres");
            txtSenha.requestFocus();
            return false;
        }

        return true;
    }
}