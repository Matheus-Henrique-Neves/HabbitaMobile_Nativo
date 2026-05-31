package com.example.habbitamobile_nativo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Home extends BaseActivity {

    private RecyclerView recyclerImoveis;
    private ProgressBar progressBar;
    private ProgressBar progressBarMais;
    private TextView txtErro;
    private TextView txtLocalizacao;
    private FusedLocationProviderClient fusedLocationClient;

    private final HashSet<String> favoritados = new HashSet<>();
    private PropertyAdapter adapter;
    private int paginaAtual = 1;
    private boolean carregandoMais = false;
    private boolean temMais = true;

    private final ActivityResultLauncher<String[]> solicitarPermissaoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                boolean concedida = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (concedida) buscarLocalizacao();
                else txtLocalizacao.setText("Localizacao nao permitida");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_home, findViewById(R.id.container));

        initViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setSelectedNavItem(R.id.navigation_home);
        verificarPermissaoLocalizacao();
        carregarFavoritos();
    }

    private void initViews() {
        recyclerImoveis = findViewById(R.id.recyclerImoveis);
        progressBar = findViewById(R.id.progressBar);
        progressBarMais = findViewById(R.id.progressBarMais);
        txtErro = findViewById(R.id.txtErro);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerImoveis.setLayoutManager(layoutManager);

        recyclerImoveis.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void verificarPermissaoLocalizacao() {
        boolean fineOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fineOk || coarseOk) buscarLocalizacao();
        else solicitarPermissaoLocalizacao.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @SuppressWarnings("MissingPermission")
    private void buscarLocalizacao() {
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) { txtLocalizacao.setText("Localizacao indisponivel"); return; }
                    reverterCoordenadas(location.getLatitude(), location.getLongitude());
                })
                .addOnFailureListener(e -> txtLocalizacao.setText("Erro ao obter localizacao"));
    }

    private void reverterCoordenadas(double lat, double lng) {
        if (!Geocoder.isPresent()) {
            txtLocalizacao.setText(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
            return;
        }
        new Geocoder(this, Locale.getDefault()).getFromLocation(lat, lng, 1, new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(List<Address> addresses) {
                if (addresses.isEmpty()) { runOnUiThread(() -> txtLocalizacao.setText("Cidade desconhecida")); return; }
                Address a = addresses.get(0);
                String cidade = a.getLocality();
                if (cidade == null) cidade = a.getSubAdminArea();
                String estado = a.getAdminArea();
                String texto = (cidade != null ? cidade : "") + (estado != null ? ", " + estado : "");
                runOnUiThread(() -> txtLocalizacao.setText(texto.isEmpty() ? "Localizado" : texto));
            }
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> txtLocalizacao.setText("Erro de geocodificacao"));
            }
        });
    }

    private void carregarFavoritos() {
        ApiService.getInstance().buscarFavoritos(new ApiService.BuscarFavoritosCallback() {
            @Override
            public void onSucesso(List<String> ids) {
                favoritados.addAll(ids);
                runOnUiThread(() -> carregarPrimeiraPagina());
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> carregarPrimeiraPagina());
            }
        });
    }

    private void carregarPrimeiraPagina() {
        paginaAtual = 1;
        temMais = true;
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
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressBarMais.setVisibility(View.GONE);
                    carregandoMais = false;

                    if (properties.isEmpty() && paginaAtual == 1) {
                        txtErro.setVisibility(View.VISIBLE);
                        txtErro.setText("Nenhum imovel disponivel.");
                        return;
                    }

                    if (paginaAtual == 1) {
                        adapter = new PropertyAdapter(properties, favoritados, Home.this::toggleFavorito);
                        adapter.setOnPropertyClickListener(p -> {
                            Intent intent = new Intent(Home.this, PropertyDetails.class);
                            intent.putExtra(PropertyDetails.EXTRA_PROPERTY, p);
                            startActivity(intent);
                        });
                        recyclerImoveis.setAdapter(adapter);
                    } else if (adapter != null) {
                        adapter.adicionarItens(properties);
                    }
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

    private void toggleFavorito(String propertyId, boolean agoraFavoritado) {
        if (agoraFavoritado) {
            ApiService.getInstance().adicionarFavorito(propertyId, new ApiService.SimpleCallback() {
                @Override public void onSucesso() {}
                @Override public void onFalha(String m) {
                    runOnUiThread(() -> Toast.makeText(Home.this, "Erro ao salvar favorito", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            ApiService.getInstance().removerFavorito(propertyId, new ApiService.SimpleCallback() {
                @Override public void onSucesso() {}
                @Override public void onFalha(String m) {
                    runOnUiThread(() -> Toast.makeText(Home.this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
                }
            });
        }
    }
}
