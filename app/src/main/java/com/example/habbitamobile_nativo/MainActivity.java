package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.habbitamobile_nativo.dao.UsuarioDAO;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnTelaCadastro, btnBiometria;
    private EditText edtEmail, edtSenha;
    private CheckBox checkBoxLembrar;
    private TextView lblMensagem;
    private UsuarioDAO usuarioDAO;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initObjects();
        loadSavedCredentials();
        verificarBiometria();
        setupListeners();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnTelaCadastro = findViewById(R.id.btnCadastro);
        btnBiometria = findViewById(R.id.btnBiometria);
        edtEmail = findViewById(R.id.textEmail);
        edtSenha = findViewById(R.id.textSenha);
        checkBoxLembrar = findViewById(R.id.checkBox);
        lblMensagem = findViewById(R.id.lblMensagem);
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

    private void verificarBiometria() {
        boolean temCredenciais = preferences.getBoolean("lembrar_me", false);
        if (!temCredenciais) {
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        int resultado = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        if (resultado == BiometricManager.BIOMETRIC_SUCCESS) {
            edtEmail.setText("");
            edtSenha.setText("");
            btnBiometria.setVisibility(View.VISIBLE);
            mostrarPromptBiometrico();
        }
    }

    private void mostrarPromptBiometrico() {
        lblMensagem.setText("");

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticacao Biometrica")
                .setSubtitle("Use sua digital para entrar")
                .setNegativeButtonText("Usar Senha")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        abrirTelaMenu();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        lblMensagem.setText("Biometria nao reconhecida");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        switch (errorCode) {
                            case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                                break;
                            case BiometricPrompt.ERROR_LOCKOUT:
                                lblMensagem.setText("Muitas tentativas. Use sua senha.");
                                break;
                            case BiometricPrompt.ERROR_LOCKOUT_PERMANENT:
                                lblMensagem.setText("Biometria bloqueada. Contate o suporte.");
                                break;
                            default:
                                lblMensagem.setText(errString.toString());
                                break;
                        }
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> realizarLogin());
        btnTelaCadastro.setOnClickListener(v -> abrirTelaCadastro());
        btnBiometria.setOnClickListener(v -> mostrarPromptBiometrico());
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (!validarCampos(email, senha)) {
            return;
        }

        boolean loginValido = usuarioDAO.login(email, senha);

        if (loginValido) {
            String nome = usuarioDAO.buscarNome(email);
            salvarPreferencias(email, senha, nome);
            abrirTelaMenu();
        } else {
            lblMensagem.setText("Email ou senha invalidos");
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

    private void salvarPreferencias(String email, String senha, String nome) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("nome", nome);
        if (checkBoxLembrar.isChecked()) {
            editor.putBoolean("lembrar_me", true);
            editor.putString("senha", senha);
        } else {
            editor.putBoolean("lembrar_me", false);
            editor.remove("senha");
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
