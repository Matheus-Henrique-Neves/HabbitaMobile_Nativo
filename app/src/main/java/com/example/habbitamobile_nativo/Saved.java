package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Saved extends BaseActivity {

    private RecyclerView recyclerSaved;
    private ProgressBar progressBar;
    private TextView txtErro;
    private PropertyAdapter adapter;
    private final HashSet<String> favoritados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_saved, findViewById(R.id.container));

        initViews();
        setupToolbar();
        setupWindowInsets();
        setSelectedNavItem(R.id.navigation_saved);
        carregarFavoritos();
    }

    private void initViews() {
        recyclerSaved = findViewById(R.id.recyclerSaved);
        progressBar = findViewById(R.id.progressBar);
        txtErro = findViewById(R.id.txtErro);
        recyclerSaved.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Imoveis Salvos");
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void carregarFavoritos() {
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);

        ApiService.getInstance().buscarFavoritos(new ApiService.BuscarFavoritosCallback() {
            @Override
            public void onSucesso(List<String> ids) {
                if (ids.isEmpty()) {
                    runOnUiThread(() -> mostrarVazio());
                    return;
                }
                favoritados.addAll(ids);
                carregarPropriedades(ids);
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarErro("Erro ao carregar favoritos: " + mensagem);
                });
            }
        });
    }

    private void carregarPropriedades(List<String> ids) {
        List<Property> properties = new ArrayList<>();
        List<String> idsList = new ArrayList<>(ids);
        int[] concluidos = {0};

        for (String id : idsList) {
            ApiService.getInstance().buscarImovelPorId(id, new ApiService.BuscarImoveisCallback() {
                @Override
                public void onSucesso(List<Property> resultado) {
                    if (!resultado.isEmpty()) properties.add(resultado.get(0));
                    concluidos[0]++;
                    if (concluidos[0] == idsList.size()) {
                        runOnUiThread(() -> exibirPropriedades(properties));
                    }
                }
                @Override
                public void onFalha(String mensagem) {
                    concluidos[0]++;
                    if (concluidos[0] == idsList.size()) {
                        runOnUiThread(() -> exibirPropriedades(properties));
                    }
                }
            });
        }
    }

    private void exibirPropriedades(List<Property> properties) {
        progressBar.setVisibility(View.GONE);
        if (properties.isEmpty()) {
            mostrarVazio();
            return;
        }
        adapter = new PropertyAdapter(properties, favoritados, (propertyId, agoraFavoritado) -> {
            ApiService.getInstance().removerFavorito(propertyId, new ApiService.SimpleCallback() {
                @Override public void onSucesso() {
                    runOnUiThread(() -> {
                        adapter.removerItem(propertyId);
                        if (adapter.getItemCount() == 0) mostrarVazio();
                    });
                }
                @Override public void onFalha(String m) {
                    runOnUiThread(() -> Toast.makeText(Saved.this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
                }
            });
        });
        recyclerSaved.setAdapter(adapter);
        recyclerSaved.setVisibility(View.VISIBLE);
    }

    private void mostrarVazio() {
        progressBar.setVisibility(View.GONE);
        recyclerSaved.setVisibility(View.GONE);
        txtErro.setVisibility(View.VISIBLE);
        txtErro.setText("Nenhum imovel salvo ainda.");
    }

    private void mostrarErro(String mensagem) {
        txtErro.setVisibility(View.VISIBLE);
        txtErro.setText(mensagem);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
