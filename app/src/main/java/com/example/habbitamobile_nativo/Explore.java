package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
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
    private ProgressBar progressBarMais;
    private TextView txtErro;
    private TextInputEditText edtBusca;
    private ChipGroup chipGroup;
    private ImageButton btnFiltros;
    private FloatingActionButton fabQrCode;

    private final List<Property> todasCarregadas = new ArrayList<>();
    private final HashSet<String> favoritados = new HashSet<>();
    private PropertyAdapter adapter;

    private int paginaAtual = 1;
    private boolean carregandoMais = false;
    private boolean temMais = true;
    private String filtroTipo = "todos";
    private String termoBusca = "";

    private double precoMin = 0;
    private double precoMax = 0;
    private int quartosMin = 0;
    private int banheirosMin = 0;

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
        progressBarMais = findViewById(R.id.progressBarMais);
        txtErro = findViewById(R.id.txtErro);
        edtBusca = findViewById(R.id.edtBusca);
        chipGroup = findViewById(R.id.chipGroup);
        btnFiltros = findViewById(R.id.btnFiltros);
        fabQrCode = findViewById(R.id.fabQrCode);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerExplore.setLayoutManager(layoutManager);

        recyclerExplore.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || carregandoMais || !temMais) return;
                int total = layoutManager.getItemCount();
                int ultimo = layoutManager.findLastVisibleItemPosition();
                if (ultimo >= total - 3) {
                    paginaAtual++;
                    carregarPagina();
                }
            }
        });
    }

    private void initObjects() {
        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

        btnFiltros.setOnClickListener(v -> abrirFiltros());
        fabQrCode.setOnClickListener(v -> abrirScanner());
    }

    private void abrirFiltros() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_filtros, null);
        dialog.setContentView(view);

        TextInputEditText edtPrecoMin = view.findViewById(R.id.edtPrecoMin);
        TextInputEditText edtPrecoMax = view.findViewById(R.id.edtPrecoMax);
        ChipGroup chipGroupQuartos = view.findViewById(R.id.chipGroupQuartos);
        ChipGroup chipGroupBanheiros = view.findViewById(R.id.chipGroupBanheiros);
        Button btnLimpar = view.findViewById(R.id.btnLimparFiltros);
        Button btnAplicar = view.findViewById(R.id.btnAplicarFiltros);

        if (precoMin > 0) edtPrecoMin.setText(String.valueOf((long) precoMin));
        if (precoMax > 0) edtPrecoMax.setText(String.valueOf((long) precoMax));
        marcarChipQuartos(chipGroupQuartos, quartosMin);
        marcarChipBanheiros(chipGroupBanheiros, banheirosMin);

        btnLimpar.setOnClickListener(v -> {
            edtPrecoMin.setText("");
            edtPrecoMax.setText("");
            ((Chip) view.findViewById(R.id.chipQuartosQualquer)).setChecked(true);
            ((Chip) view.findViewById(R.id.chipBanheirosQualquer)).setChecked(true);
        });

        btnAplicar.setOnClickListener(v -> {
            precoMin = parseValor(edtPrecoMin);
            precoMax = parseValor(edtPrecoMax);
            quartosMin = lerChipQuartos(chipGroupQuartos);
            banheirosMin = lerChipBanheiros(chipGroupBanheiros);
            atualizarIndicadorFiltros();
            filtrarEAtualizar();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void marcarChipQuartos(ChipGroup group, int valor) {
        int id = valor == 1 ? R.id.chipQuartos1 : valor == 2 ? R.id.chipQuartos2
                : valor == 3 ? R.id.chipQuartos3 : valor >= 4 ? R.id.chipQuartos4 : R.id.chipQuartosQualquer;
        ((Chip) group.findViewById(id)).setChecked(true);
    }

    private void marcarChipBanheiros(ChipGroup group, int valor) {
        int id = valor == 1 ? R.id.chipBanheiros1 : valor == 2 ? R.id.chipBanheiros2
                : valor >= 3 ? R.id.chipBanheiros3 : R.id.chipBanheirosQualquer;
        ((Chip) group.findViewById(id)).setChecked(true);
    }

    private int lerChipQuartos(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chipQuartos1) return 1;
        if (id == R.id.chipQuartos2) return 2;
        if (id == R.id.chipQuartos3) return 3;
        if (id == R.id.chipQuartos4) return 4;
        return 0;
    }

    private int lerChipBanheiros(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id == R.id.chipBanheiros1) return 1;
        if (id == R.id.chipBanheiros2) return 2;
        if (id == R.id.chipBanheiros3) return 3;
        return 0;
    }

    private double parseValor(TextInputEditText campo) {
        String texto = campo.getText() != null ? campo.getText().toString().trim() : "";
        if (texto.isEmpty()) return 0;
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean temFiltrosAtivos() {
        return precoMin > 0 || precoMax > 0 || quartosMin > 0 || banheirosMin > 0;
    }

    private void atualizarIndicadorFiltros() {
        btnFiltros.setColorFilter(temFiltrosAtivos() ? 0xFF4CAF50 : 0xFFFFFFFF);
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

        for (Property p : todasCarregadas) {
            if (id.equals(p.getId())) { abrirDetalhes(p); return; }
        }

        ApiService.getInstance().buscarImovelPorId(id, new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties, boolean hasMore) {
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
            @Override public void onSucesso(List<String> ids) { favoritados.addAll(ids); runOnUiThread(() -> carregarPrimeiraPagina()); }
            @Override public void onFalha(String mensagem) { runOnUiThread(() -> carregarPrimeiraPagina()); }
        });
    }

    private void carregarPrimeiraPagina() {
        paginaAtual = 1;
        temMais = true;
        todasCarregadas.clear();
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);
        carregarPagina();
    }

    private void carregarPagina() {
        if (carregandoMais) return;
        carregandoMais = true;

        if (paginaAtual > 1) progressBarMais.setVisibility(View.VISIBLE);

        ApiService.getInstance().buscarImoveis(paginaAtual, new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties, boolean hasMore) {
                temMais = hasMore;
                todasCarregadas.addAll(properties);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressBarMais.setVisibility(View.GONE);
                    carregandoMais = false;
                    filtrarEAtualizar();
                });
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressBarMais.setVisibility(View.GONE);
                    carregandoMais = false;
                    if (paginaAtual == 1) {
                        txtErro.setVisibility(View.VISIBLE);
                        txtErro.setText("Erro ao carregar imoveis: " + mensagem);
                    }
                });
            }
        });
    }

    private void filtrarEAtualizar() {
        List<Property> filtradas = new ArrayList<>();
        for (Property p : todasCarregadas) {
            if (passaFiltros(p)) filtradas.add(p);
        }

        if (filtradas.isEmpty() && !todasCarregadas.isEmpty()) {
            txtErro.setVisibility(View.VISIBLE);
            txtErro.setText("Nenhum imovel encontrado para essa busca.");
            recyclerExplore.setAdapter(null);
            adapter = null;
        } else if (!filtradas.isEmpty()) {
            txtErro.setVisibility(View.GONE);
            adapter = new PropertyAdapter(filtradas, favoritados, this::toggleFavorito);
            adapter.setOnPropertyClickListener(this::abrirDetalhes);
            recyclerExplore.setAdapter(adapter);
        }
    }

    private boolean passaFiltros(Property p) {
        boolean passaTipo = filtroTipo.equals("todos")
                || (p.getTransactionType() != null && p.getTransactionType().equalsIgnoreCase(filtroTipo));

        boolean passaBusca = termoBusca.isEmpty()
                || contemTermo(p.getTitle(), termoBusca)
                || contemTermo(p.getAddress(), termoBusca)
                || contemTermo(p.getDescription(), termoBusca);

        boolean passaPrecoMin = precoMin <= 0 || p.getPrice() >= precoMin;
        boolean passaPrecoMax = precoMax <= 0 || p.getPrice() <= precoMax;
        boolean passaQuartos = quartosMin <= 0 || p.getBedrooms() >= quartosMin;
        boolean passaBanheiros = banheirosMin <= 0 || p.getBathrooms() >= banheirosMin;

        return passaTipo && passaBusca && passaPrecoMin && passaPrecoMax && passaQuartos && passaBanheiros;
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
