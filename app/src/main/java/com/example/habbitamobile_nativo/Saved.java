package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Saved extends BaseActivity {

    private TextView textViewSavedCount;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar o layout específico no container
        getLayoutInflater().inflate(R.layout.activity_saved, findViewById(R.id.container));

        initViews();
        setupToolbar();
        setupWindowInsets();
        loadSavedProperties();

        // Selecionar o item correto no menu
        setSelectedNavItem(R.id.navigation_saved);
    }

    private void initViews() {
        textViewSavedCount = findViewById(R.id.textView8);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Imóveis Salvos");
        }
    }

    private void loadSavedProperties() {
        int savedCount = 0;
        textViewSavedCount.setText(savedCount + " imóvel(s) salvo(s)");

        if (savedCount == 0) {
            Toast.makeText(this, "Nenhum imóvel salvo ainda", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}