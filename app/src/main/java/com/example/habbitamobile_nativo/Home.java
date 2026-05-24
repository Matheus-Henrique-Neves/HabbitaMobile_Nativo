package com.example.habbitamobile_nativo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends BaseActivity {

    private RecyclerView recyclerImoveis;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextView txtLocalizacao;
    private FirebaseFirestore firestore;
    private FusedLocationProviderClient fusedLocationClient;

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
        carregarImoveis();
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
                .addOnFailureListener(e ->
                        txtLocalizacao.setText("Erro ao obter localizacao"));
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

    private void carregarImoveis() {
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);

        firestore.collection("properties")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Property> properties = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        properties.add(documentParaProperty(doc));
                    }

                    progressBar.setVisibility(View.GONE);

                    if (properties.isEmpty()) {
                        txtErro.setVisibility(View.VISIBLE);
                        txtErro.setText("Nenhum imovel cadastrado ainda.");
                    } else {
                        recyclerImoveis.setAdapter(new PropertyAdapter(properties));
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    txtErro.setVisibility(View.VISIBLE);
                    txtErro.setText("Erro ao carregar imoveis: " + e.getMessage());
                });
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
