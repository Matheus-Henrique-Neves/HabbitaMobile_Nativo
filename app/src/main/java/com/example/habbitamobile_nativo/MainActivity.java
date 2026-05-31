package com.example.habbitamobile_nativo;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin, btnTelaCadastro, btnBiometria;
    private EditText edtEmail, edtSenha;
    private CheckBox checkBoxLembrar;
    private TextView lblMensagem;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();

        FirebaseUser usuarioAtual = auth.getCurrentUser();
        if (usuarioAtual != null) {
            verificarBiometria();
        }
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

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> realizarLogin());
        btnTelaCadastro.setOnClickListener(v -> abrirTelaCadastro());
        btnBiometria.setOnClickListener(v -> mostrarPromptBiometrico());
    }

    private void verificarBiometria() {
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
        lblMensagem.setVisibility(View.GONE);

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
                        lblMensagem.setVisibility(View.VISIBLE);
                        lblMensagem.setText("Biometria nao reconhecida");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        switch (errorCode) {
                            case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                                lblMensagem.setVisibility(View.GONE);
                                break;
                            case BiometricPrompt.ERROR_LOCKOUT:
                                lblMensagem.setVisibility(View.VISIBLE);
                                lblMensagem.setText("Muitas tentativas. Use sua senha.");
                                break;
                            case BiometricPrompt.ERROR_LOCKOUT_PERMANENT:
                                lblMensagem.setVisibility(View.VISIBLE);
                                lblMensagem.setText("Biometria bloqueada. Contate o suporte.");
                                break;
                            default:
                                lblMensagem.setVisibility(View.VISIBLE);
                                lblMensagem.setText(errString.toString());
                                break;
                        }
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    private void realizarLogin() {
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (email.isEmpty()) {
            edtEmail.setError("Digite seu email");
            edtEmail.requestFocus();
            return;
        }
        if (senha.isEmpty()) {
            edtSenha.setError("Digite sua senha");
            edtSenha.requestFocus();
            return;
        }

        lblMensagem.setText("");
        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener(result -> abrirTelaMenu())
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    lblMensagem.setVisibility(View.VISIBLE);
                    lblMensagem.setText("Email ou senha invalidos");
                    edtEmail.requestFocus();
                });
    }

    private void abrirTelaCadastro() {
        startActivity(new Intent(MainActivity.this, RegisterUser.class));
    }

    public void abrirTelaMenu() {
        Intent intent = new Intent(MainActivity.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
