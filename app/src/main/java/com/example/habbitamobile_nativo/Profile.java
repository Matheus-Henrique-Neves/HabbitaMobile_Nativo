package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Profile extends BaseActivity {

    private TextView userName, userEmail;
    private LinearLayout menuPersonalInfo, menuEmailPassword, menuNotifications, menuHelp;
    private Button btnRegisterProperty, btnLogout;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar o layout específico no container
        getLayoutInflater().inflate(R.layout.activity_profile, findViewById(R.id.container));

        initViews();
        setupWindowInsets();
        loadUserData();
        setupListeners();

        // Selecionar o item correto no menu
        setSelectedNavItem(R.id.navigation_profile);
    }

    private void initViews() {
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        menuPersonalInfo = findViewById(R.id.menuPersonalInfo);
        menuEmailPassword = findViewById(R.id.menuEmailPassword);
        menuNotifications = findViewById(R.id.menuNotifications);
        menuHelp = findViewById(R.id.menuHelp);
        btnRegisterProperty = findViewById(R.id.btnRegisterProperty);
        btnLogout = findViewById(R.id.btnLogout);

        preferences = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        String email = preferences.getString("email", "usuario@email.com");
        userEmail.setText(email);

        String nome = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        userName.setText(nome);
    }

    private void setupListeners() {
        menuPersonalInfo.setOnClickListener(v -> {
            Toast.makeText(this, "Informações Pessoais", Toast.LENGTH_SHORT).show();
        });

        menuEmailPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Alterar Email/Senha", Toast.LENGTH_SHORT).show();
        });

        menuNotifications.setOnClickListener(v -> {
            Toast.makeText(this, "Configurações de Notificação", Toast.LENGTH_SHORT).show();
        });

        menuHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Central de Ajuda", Toast.LENGTH_SHORT).show();
        });

        btnRegisterProperty.setOnClickListener(v -> {
            Toast.makeText(this, "Cadastrar novo imóvel", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            realizarLogout();
        });
    }

    private void realizarLogout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(Profile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}