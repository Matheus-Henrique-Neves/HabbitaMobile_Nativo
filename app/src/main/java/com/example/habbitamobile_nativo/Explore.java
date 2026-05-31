package com.example.habbitamobile_nativo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Explore extends BaseActivity {

    private RecyclerView recyclerExplore;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextInputEditText edtBusca;
    private ChipGroup chipGroup;
    private FirebaseFirestore firestore;

    private final List<Property> todasPropriedades = new ArrayList<>();
    private final HashSet<String> favoritados = new HashSet<>();
    private final AtomicInteger fontesPendentes = new AtomicInteger(0);
    private String filtroTipo = "todos";
    private String termoBusca = "";

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
        recyclerExplore.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initObjects() {
        firestore = FirebaseFirestore.getInstance();

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
            if (id == R.id.chipVendas) {
                filtroTipo = "sell";
            } else if (id == R.id.chipAlugueis) {
                filtroTipo = "rent";
            } else {
                filtroTipo = "todos";
            }
            filtrarEAtualizar();
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
        todasPropriedades.clear();
        fontesPendentes.set(2);

        carregarDaApi();
        carregarDoFirestore();
    }

    private void carregarDaApi() {
        ApiService.getInstance().buscarImoveis(new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties) {
                todasPropriedades.addAll(properties);
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
                        todasPropriedades.add(documentParaProperty(doc));
                    }
                    verificarConclusao();
                })
                .addOnFailureListener(e -> verificarConclusao());
    }

    private void verificarConclusao() {
        if (fontesPendentes.decrementAndGet() == 0) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                filtrarEAtualizar();
            });
        }
    }

    private void filtrarEAtualizar() {
        List<Property> filtradas = new ArrayList<>();

        for (Property p : todasPropriedades) {
            boolean passaTipo = filtroTipo.equals("todos")
                    || (p.getTransactionType() != null
                    && p.getTransactionType().equalsIgnoreCase(filtroTipo));

            boolean passaBusca = termoBusca.isEmpty()
                    || contemTermo(p.getTitle(), termoBusca)
                    || contemTermo(p.getAddress(), termoBusca)
                    || contemTermo(p.getDescription(), termoBusca);

            if (passaTipo && passaBusca) {
                filtradas.add(p);
            }
        }

        if (filtradas.isEmpty() && !todasPropriedades.isEmpty()) {
            txtErro.setVisibility(View.VISIBLE);
            txtErro.setText("Nenhum imovel encontrado para essa busca.");
            recyclerExplore.setAdapter(null);
        } else if (!filtradas.isEmpty()) {
            txtErro.setVisibility(View.GONE);
            recyclerExplore.setAdapter(new PropertyAdapter(filtradas, favoritados, this::toggleFavorito));
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

    private boolean contemTermo(String campo, String termo) {
        return campo != null && campo.toLowerCase().contains(termo);
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
