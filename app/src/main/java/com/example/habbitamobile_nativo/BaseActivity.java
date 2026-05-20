package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected int currentSelectedItem = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Configurar cores do BottomNavigationView (apenas preto, branco e cinza)
        setupBottomNavigationColors();
        setupBottomNavigation();
    }

    private void setupBottomNavigationColors() {
        // Configurar cores para os ícones e textos
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked},  // Item selecionado
                new int[] {-android.R.attr.state_checked}, // Item não selecionado
        };

        int[] colors = new int[] {
                ContextCompat.getColor(this, R.color.bottom_nav_selected),   // Preto para selecionado
                ContextCompat.getColor(this, R.color.bottom_nav_unselected), // Cinza para não selecionado
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);

        // Aplicar cores aos ícones e textos
        bottomNavigationView.setItemIconTintList(colorStateList);
        bottomNavigationView.setItemTextColor(colorStateList);

        // Configurar fundo branco
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.bottom_nav_background));

        // Opcional: Adicionar uma sombra/sublinha para o item selecionado (efeito visual)
        bottomNavigationView.setItemBackgroundResource(android.R.color.transparent);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    openHome();
                    return true;
                } else if (itemId == R.id.navigation_explore) {
                    openExplore();
                    return true;
                } else if (itemId == R.id.navigation_saved) {
                    openSaved();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    openProfile();
                    return true;
                }
                return false;
            }
        });
    }

    protected void openHome() {
        if (!(this instanceof Home)) {
            Intent intent = new Intent(this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    protected void openExplore() {
        if (!(this instanceof Explore)) {
            Intent intent = new Intent(this, Explore.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    protected void openSaved() {
        if (!(this instanceof Saved)) {
            Intent intent = new Intent(this, Saved.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    protected void openProfile() {
        if (!(this instanceof Profile)) {
            Intent intent = new Intent(this, Profile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    protected void setSelectedNavItem(int itemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }
}