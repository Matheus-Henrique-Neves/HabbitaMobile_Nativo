package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habbitamobile_nativo.dao.UsuarioDAO;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnTelaCadastro;
    private EditText edtEmail, edtSenha;
    private CheckBox checkBoxLembrar;
    private UsuarioDAO usuarioDAO;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initObjects();
        loadSavedCredentials();
        setupListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnTelaCadastro = findViewById(R.id.btnCadastro);
        edtEmail = findViewById(R.id.textEmail);
        edtSenha = findViewById(R.id.textSenha);
        checkBoxLembrar = findViewById(R.id.checkBox);
    }

    private void initObjects() {
        usuarioDAO = new UsuarioDAO(this);
        preferences = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
    }

    private void loadSavedCredentials() {
        boolean lembrar = preferences.getBoolean("lembrar_me", false);
        if (lembrar) {
            String email = preferences.getString("email", "");
            String senha = preferences.getString("senha", "");
            edtEmail.setText(email);
            edtSenha.setText(senha);
            checkBoxLembrar.setChecked(true);
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> realizarLogin());
        btnTelaCadastro.setOnClickListener(v -> abrirTelaCadastro());
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (!validarCampos(email, senha)) {
            return;
        }

        boolean loginValido = usuarioDAO.login(email, senha);

        if (loginValido) {
            salvarPreferencias(email, senha);
            abrirTelaMenu();
        } else {
            Toast.makeText(this, "Email ou senha inválidos", Toast.LENGTH_LONG).show();
            edtEmail.requestFocus();
        }
    }

    private boolean validarCampos(String email, String senha) {
        if (email.isEmpty()) {
            edtEmail.setError("Digite seu email");
            edtEmail.requestFocus();
            return false;
        }

        if (senha.isEmpty()) {
            edtSenha.setError("Digite sua senha");
            edtSenha.requestFocus();
            return false;
        }

        return true;
    }

    private void salvarPreferencias(String email, String senha) {
        SharedPreferences.Editor editor = preferences.edit();
        if (checkBoxLembrar.isChecked()) {
            editor.putBoolean("lembrar_me", true);
            editor.putString("email", email);
            editor.putString("senha", senha);
        } else {
            editor.clear();
        }
        editor.apply();
    }

    public void abrirTelaCadastro() {
        Intent telaCadastro = new Intent(MainActivity.this, RegisterUser.class);
        startActivity(telaCadastro);
    }

    public void abrirTelaMenu() {
        Intent telaMenu = new Intent(MainActivity.this, Home.class);
        telaMenu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(telaMenu);
        finish();
    }
}