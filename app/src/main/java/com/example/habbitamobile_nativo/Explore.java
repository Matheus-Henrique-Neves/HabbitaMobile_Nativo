package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Explore extends BaseActivity {

    private RecyclerView recyclerExplore;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextInputEditText edtBusca;
    private ChipGroup chipGroup;
    private FloatingActionButton fabQrCode;

    private final List<Property> todasPropriedades = new ArrayList<>();
    private final HashSet<String> favoritados = new HashSet<>();
    private String filtroTipo = "todos";
    private String termoBusca = "";

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> scannerLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) buscarImovelPorQrCode(result.getContents());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_explore, findViewById(R.id.container));

        initViews();
        initObjects();
        setSelectedNavItem(R.id.navigation_explore);
        carregarFavoritos();
    }

    private void initViews() {
        recyclerExplore = findViewById(R.id.recyclerExplore);
        progressBar = findViewById(R.id.progressBar);
        txtErro = findViewById(R.id.txtErro);
        edtBusca = findViewById(R.id.edtBusca);
        chipGroup = findViewById(R.id.chipGroup);
        fabQrCode = findViewById(R.id.fabQrCode);
        recyclerExplore.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initObjects() {
        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                termoBusca = s.toString().toLowerCase().trim();
                filtrarEAtualizar();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            filtroTipo = id == R.id.chipVendas ? "sell" : id == R.id.chipAlugueis ? "rent" : "todos";
            filtrarEAtualizar();
        });

        fabQrCode.setOnClickListener(v -> abrirScanner());
    }

    private void abrirScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Aponte para o QR Code do imovel");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        scannerLauncher.launch(options);
    }

    private void buscarImovelPorQrCode(String conteudo) {
        String id = extrairId(conteudo);

        for (Property p : todasPropriedades) {
            if (id.equals(p.getId())) { abrirDetalhes(p); return; }
        }

        ApiService.getInstance().buscarImovelPorId(id, new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties) {
                runOnUiThread(() -> {
                    if (!properties.isEmpty()) abrirDetalhes(properties.get(0));
                    else Toast.makeText(Explore.this, "Imovel nao encontrado", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> Toast.makeText(Explore.this, "Erro ao buscar imovel", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String extrairId(String conteudo) {
        if (conteudo.contains("/")) {
            String[] partes = conteudo.split("/");
            return partes[partes.length - 1].trim();
        }
        return conteudo.trim();
    }

    private void abrirDetalhes(Property p) {
        Intent intent = new Intent(Explore.this, PropertyDetails.class);
        intent.putExtra(PropertyDetails.EXTRA_PROPERTY, p);
        startActivity(intent);
    }

    private void carregarFavoritos() {
        ApiService.getInstance().buscarFavoritos(new ApiService.BuscarFavoritosCallback() {
            @Override public void onSucesso(List<String> ids) { favoritados.addAll(ids); runOnUiThread(() -> carregarImoveis()); }
            @Override public void onFalha(String mensagem) { runOnUiThread(() -> carregarImoveis()); }
        });
    }

    private void carregarImoveis() {
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);
        todasPropriedades.clear();

        ApiService.getInstance().buscarImoveis(new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties) {
                todasPropriedades.addAll(properties);
                runOnUiThread(() -> { progressBar.setVisibility(View.GONE); filtrarEAtualizar(); });
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    txtErro.setVisibility(View.VISIBLE);
                    txtErro.setText("Erro ao carregar imoveis: " + mensagem);
                });
            }
        });
    }

    private void filtrarEAtualizar() {
        List<Property> filtradas = new ArrayList<>();
        for (Property p : todasPropriedades) {
            boolean passaTipo = filtroTipo.equals("todos")
                    || (p.getTransactionType() != null && p.getTransactionType().equalsIgnoreCase(filtroTipo));
            boolean passaBusca = termoBusca.isEmpty()
                    || contemTermo(p.getTitle(), termoBusca)
                    || contemTermo(p.getAddress(), termoBusca)
                    || contemTermo(p.getDescription(), termoBusca);
            if (passaTipo && passaBusca) filtradas.add(p);
        }

        if (filtradas.isEmpty() && !todasPropriedades.isEmpty()) {
            txtErro.setVisibility(View.VISIBLE);
            txtErro.setText("Nenhum imovel encontrado para essa busca.");
            recyclerExplore.setAdapter(null);
        } else if (!filtradas.isEmpty()) {
            txtErro.setVisibility(View.GONE);
            PropertyAdapter adapter = new PropertyAdapter(filtradas, favoritados, this::toggleFavorito);
            adapter.setOnPropertyClickListener(p -> {
                Intent intent = new Intent(Explore.this, PropertyDetails.class);
                intent.putExtra(PropertyDetails.EXTRA_PROPERTY, p);
                startActivity(intent);
            });
            recyclerExplore.setAdapter(adapter);
        }
    }

    private void toggleFavorito(String propertyId, boolean agoraFavoritado) {
        if (agoraFavoritado) {
            ApiService.getInstance().adicionarFavorito(propertyId, new ApiService.SimpleCallback() {
                @Override public void onSucesso() {}
                @Override public void onFalha(String m) {
                    runOnUiThread(() -> Toast.makeText(Explore.this, "Erro ao salvar favorito", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            ApiService.getInstance().removerFavorito(propertyId, new ApiService.SimpleCallback() {
                @Override public void onSucesso() {}
                @Override public void onFalha(String m) {
                    runOnUiThread(() -> Toast.makeText(Explore.this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private boolean contemTermo(String campo, String termo) {
        return campo != null && campo.toLowerCase().contains(termo);
    }
}
