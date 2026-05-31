package com.example.habbitamobile_nativo;

import android.Manifest;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Home extends BaseActivity {

    private RecyclerView recyclerImoveis;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextView txtLocalizacao;
    private FirebaseFirestore firestore;
    private FusedLocationProviderClient fusedLocationClient;
    private final HashSet<String> favoritados = new HashSet<>();

    private final List<Property> propriedadesApi = new ArrayList<>();
    private final List<Property> propriedadesFirestore = new ArrayList<>();
    private final AtomicInteger fontesPendentes = new AtomicInteger(0);

    private final ActivityResultLauncher<String[]> solicitarPermissaoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                boolean concedida = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (concedida) {
                    buscarLocalizacao();
                } else {
                    txtLocalizacao.setText("Localizacao nao permitida");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_home, findViewById(R.id.container));

        initViews();
        initObjects();
        setSelectedNavItem(R.id.navigation_home);
        verificarPermissaoLocalizacao();
        carregarFavoritos();
    }

    private void initViews() {
        recyclerImoveis = findViewById(R.id.recyclerImoveis);
        progressBar = findViewById(R.id.progressBar);
        txtErro = findViewById(R.id.txtErro);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        recyclerImoveis.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initObjects() {
        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void verificarPermissaoLocalizacao() {
        boolean fineOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (fineOk || coarseOk) {
            buscarLocalizacao();
        } else {
            solicitarPermissaoLocalizacao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void buscarLocalizacao() {
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        txtLocalizacao.setText("Localizacao indisponivel");
                        return;
                    }
                    reverterCoordenadas(location.getLatitude(), location.getLongitude());
                })
                .addOnFailureListener(e -> txtLocalizacao.setText("Erro ao obter localizacao"));
    }

    private void reverterCoordenadas(double lat, double lng) {
        if (!Geocoder.isPresent()) {
            txtLocalizacao.setText(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        geocoder.getFromLocation(lat, lng, 1, new Geocoder.GeocodeListener() {
            @Override
            public void onGeocode(List<Address> addresses) {
                if (addresses.isEmpty()) {
                    runOnUiThread(() -> txtLocalizacao.setText("Cidade desconhecida"));
                    return;
                }
                Address address = addresses.get(0);
                String cidade = address.getLocality();
                if (cidade == null) cidade = address.getSubAdminArea();
                String estado = address.getAdminArea();
                String texto = (cidade != null ? cidade : "") +
                        (estado != null ? ", " + estado : "");
                runOnUiThread(() -> txtLocalizacao.setText(texto.isEmpty() ? "Localizado" : texto));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> txtLocalizacao.setText("Erro de geocodificacao"));
            }
        });
    }

    private void carregarFavoritos() {
        SharedPreferences prefs = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if (email.isEmpty()) {
            carregarImoveis();
            return;
        }

        firestore.collection("favorites").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<?> ids = (List<?>) doc.get("propertyIds");
                        if (ids != null) {
                            for (Object id : ids) {
                                if (id instanceof String) favoritados.add((String) id);
                            }
                        }
                    }
                    carregarImoveis();
                })
                .addOnFailureListener(e -> carregarImoveis());
    }

    private void carregarImoveis() {
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);
        propriedadesApi.clear();
        propriedadesFirestore.clear();
        fontesPendentes.set(2);

        carregarDaApi();
        carregarDoFirestore();
    }

    private void carregarDaApi() {
        ApiService.getInstance().buscarImoveis(new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties) {
                propriedadesApi.addAll(properties);
                verificarConclusao();
            }

            @Override
            public void onFalha(String mensagem) {
                verificarConclusao();
            }
        });
    }

    private void carregarDoFirestore() {
        firestore.collection("properties")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        propriedadesFirestore.add(documentParaProperty(doc));
                    }
                    verificarConclusao();
                })
                .addOnFailureListener(e -> verificarConclusao());
    }

    private void verificarConclusao() {
        if (fontesPendentes.decrementAndGet() == 0) {
            runOnUiThread(this::exibirImoveis);
        }
    }

    private void exibirImoveis() {
        progressBar.setVisibility(View.GONE);
        List<Property> todos = new ArrayList<>();
        todos.addAll(propriedadesApi);
        todos.addAll(propriedadesFirestore);

        if (todos.isEmpty()) {
            txtErro.setVisibility(View.VISIBLE);
            txtErro.setText("Nenhum imovel disponivel.");
        } else {
            recyclerImoveis.setAdapter(new PropertyAdapter(todos, favoritados, this::toggleFavorito));
        }
    }

    private void toggleFavorito(String propertyId, boolean agoraFavoritado) {
        SharedPreferences prefs = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        if (email.isEmpty()) return;

        DocumentReference docRef = firestore.collection("favorites").document(email);
        Map<String, Object> data = new HashMap<>();

        if (agoraFavoritado) {
            data.put("propertyIds", FieldValue.arrayUnion(propertyId));
            docRef.set(data, SetOptions.merge())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar favorito", Toast.LENGTH_SHORT).show());
        } else {
            data.put("propertyIds", FieldValue.arrayRemove(propertyId));
            docRef.update(data)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
        }
    }

    private Property documentParaProperty(QueryDocumentSnapshot doc) {
        Property p = new Property();
        p.setId(doc.getId());
        p.setTitle(doc.getString("title"));
        p.setImageUrl(doc.getString("image_url"));
        p.setAddress(doc.getString("address"));
        p.setDescription(doc.getString("description"));
        p.setType(doc.getString("type"));
        p.setTransactionType(doc.getString("transactionType"));

        Double price = doc.getDouble("price");
        p.setPrice(price != null ? price : 0);

        Double area = doc.getDouble("area");
        p.setArea(area != null ? area : 0);

        Long bedrooms = doc.getLong("bedrooms");
        p.setBedrooms(bedrooms != null ? bedrooms.intValue() : 0);

        Long bathrooms = doc.getLong("bathrooms");
        p.setBathrooms(bathrooms != null ? bathrooms.intValue() : 0);

        Long garages = doc.getLong("garages");
        p.setGarages(garages != null ? garages.intValue() : 0);

        return p;
    }
}
