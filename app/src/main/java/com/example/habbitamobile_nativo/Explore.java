package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Explore extends BaseActivity {

    private Button btnExplorar, btnVendas, btnAlugueis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar o layout específico no container
        getLayoutInflater().inflate(R.layout.activity_explore, findViewById(R.id.container));

        initViews();
        setupWindowInsets();
        setupListeners();

        // Selecionar o item correto no menu
        setSelectedNavItem(R.id.navigation_explore);
    }

    private void initViews() {
        btnExplorar = findViewById(R.id.btnLogin2);
        btnVendas = findViewById(R.id.btnLogin3);
        btnAlugueis = findViewById(R.id.btnLogin4);
    }

    private void setupListeners() {
        btnExplorar.setOnClickListener(v -> {
            Toast.makeText(this, "Explorar imóveis", Toast.LENGTH_SHORT).show();
        });

        btnVendas.setOnClickListener(v -> {
            Toast.makeText(this, "Imóveis à venda", Toast.LENGTH_SHORT).show();
        });

        btnAlugueis.setOnClickListener(v -> {
            Toast.makeText(this, "Imóveis para alugar", Toast.LENGTH_SHORT).show();
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