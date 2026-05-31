package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends BaseActivity {

    private TextView userName, userEmail;
    private LinearLayout menuPersonalInfo, menuEmailPassword, menuNotifications, menuHelp;
    private Button btnRegisterProperty, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, findViewById(R.id.container));

        initViews();
        setupWindowInsets();
        loadUserData();
        setupListeners();
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
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            String nome = user.getDisplayName();
            userName.setText(nome != null && !nome.isEmpty() ? nome : "Usuario");
        }
    }

    private void setupListeners() {
        menuPersonalInfo.setOnClickListener(v ->
                Toast.makeText(this, "Informacoes Pessoais", Toast.LENGTH_SHORT).show());
        menuEmailPassword.setOnClickListener(v ->
                Toast.makeText(this, "Alterar Email/Senha", Toast.LENGTH_SHORT).show());
        menuNotifications.setOnClickListener(v ->
                Toast.makeText(this, "Configuracoes de Notificacao", Toast.LENGTH_SHORT).show());
        menuHelp.setOnClickListener(v ->
                Toast.makeText(this, "Central de Ajuda", Toast.LENGTH_SHORT).show());

        btnRegisterProperty.setOnClickListener(v ->
                startActivity(new Intent(Profile.this, RegisterProperty.class)));

        btnLogout.setOnClickListener(v -> realizarLogout());
    }

    private void realizarLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Profile.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
