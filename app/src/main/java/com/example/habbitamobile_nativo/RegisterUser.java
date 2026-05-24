package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habbitamobile_nativo.dao.UsuarioDAO;
import com.example.habbitamobile_nativo.model.Usuario;
import com.example.habbitamobile_nativo.util.SenhaUtil;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterUser extends AppCompatActivity {

    private TextInputEditText edtNome;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;
    private TextInputEditText edtConfirmarSenha;
    private TextView txtErro;
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
        edtNome = findViewById(R.id.edtNomeCadastro);
        edtEmail = findViewById(R.id.edtEmailCadastro);
        edtSenha = findViewById(R.id.edtSenhaCadastro);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenhaCadastro);
        txtErro = findViewById(R.id.txtErroCadastro);
        btnCadastrar = findViewById(R.id.btnCadastrar);
    }

    private void initObjects() {
        usuarioDAO = new UsuarioDAO(this);
    }

    private void setupListeners() {
        btnCadastrar.setOnClickListener(v -> realizarCadastro());
    }

    private void realizarCadastro() {
        esconderErro();

        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim().toLowerCase();
        String senha = edtSenha.getText().toString();
        String confirmar = edtConfirmarSenha.getText().toString();

        if (!validarCampos(nome, email, senha, confirmar)) {
            return;
        }

        if (usuarioDAO.emailExiste(email)) {
            mostrarErro("Este email ja esta cadastrado.");
            edtEmail.requestFocus();
            return;
        }

        String senhaHash = SenhaUtil.hash(senha);
        Usuario usuario = new Usuario(nome, email, senhaHash);
        boolean sucesso = usuarioDAO.inserir(usuario);

        if (sucesso) {
            setResult(RESULT_OK);
            finish();
        } else {
            mostrarErro("Erro ao cadastrar. Tente novamente.");
        }
    }

    private boolean validarCampos(String nome, String email, String senha, String confirmar) {
        if (nome.isEmpty()) {
            mostrarErro("Digite seu nome.");
            edtNome.requestFocus();
            return false;
        }

        if (nome.length() < 2) {
            mostrarErro("O nome deve ter no minimo 2 caracteres.");
            edtNome.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            mostrarErro("Digite seu email.");
            edtEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro("Digite um email valido.");
            edtEmail.requestFocus();
            return false;
        }

        if (senha.isEmpty()) {
            mostrarErro("Digite sua senha.");
            edtSenha.requestFocus();
            return false;
        }

        if (senha.length() < 6) {
            mostrarErro("A senha deve ter no minimo 6 caracteres.");
            edtSenha.requestFocus();
            return false;
        }

        if (!senha.matches(".*[a-zA-Z].*") || !senha.matches(".*[0-9].*")) {
            mostrarErro("A senha deve conter letras e numeros.");
            edtSenha.requestFocus();
            return false;
        }

        if (confirmar.isEmpty()) {
            mostrarErro("Confirme sua senha.");
            edtConfirmarSenha.requestFocus();
            return false;
        }

        if (!senha.equals(confirmar)) {
            mostrarErro("As senhas nao coincidem.");
            edtConfirmarSenha.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarErro(String mensagem) {
        txtErro.setText(mensagem);
        txtErro.setVisibility(View.VISIBLE);
    }

    private void esconderErro() {
        txtErro.setVisibility(View.GONE);
        txtErro.setText("");
    }
}
