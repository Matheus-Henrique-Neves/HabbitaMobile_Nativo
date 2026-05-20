package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home extends BaseActivity {

    private Button btnExplorar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar o layout específico no container
        getLayoutInflater().inflate(R.layout.activity_home, findViewById(R.id.container));

        initViews();
        setupWindowInsets();
        setupListeners();

        // Selecionar o item correto no menu
        setSelectedNavItem(R.id.navigation_home);
    }

    private void initViews() {
        btnExplorar = findViewById(R.id.btnExplorar);
    }

    private void setupListeners() {
        btnExplorar.setOnClickListener(v -> {
            // Isso abre a Explore activity, mas a navegação inferior ainda funcionará
            openExplore();
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}