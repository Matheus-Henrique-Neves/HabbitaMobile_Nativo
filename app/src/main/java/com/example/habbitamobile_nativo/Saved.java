package com.example.habbitamobile_nativo;

import android.content.SharedPreferences;
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
import com.example.habbitamobile_nativo.model.Property;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Saved extends BaseActivity {

    private RecyclerView recyclerSaved;
    private ProgressBar progressBar;
    private TextView txtErro;
    private FirebaseFirestore firestore;
    private PropertyAdapter adapter;
    private final HashSet<String> favoritados = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_saved, findViewById(R.id.container));

        initViews();
        setupToolbar();
        setupWindowInsets();
        initObjects();
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

    private void initObjects() {
        firestore = FirebaseFirestore.getInstance();
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

        SharedPreferences prefs = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");

        if (email.isEmpty()) {
            mostrarVazio();
            return;
        }

        firestore.collection("favorites").document(email).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        mostrarVazio();
                        return;
                    }
                    List<?> ids = (List<?>) doc.get("propertyIds");
                    if (ids == null || ids.isEmpty()) {
                        mostrarVazio();
                        return;
                    }
                    List<String> propertyIds = new ArrayList<>();
                    for (Object id : ids) {
                        if (id instanceof String) propertyIds.add((String) id);
                    }
                    favoritados.addAll(propertyIds);
                    carregarPropriedades(propertyIds);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarErro("Erro ao carregar favoritos: " + e.getMessage());
                });
    }

    private void carregarPropriedades(List<String> ids) {
        firestore.collection("properties")
                .whereIn(FieldPath.documentId(), ids)
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<Property> properties = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        properties.add(documentParaProperty(doc));
                    }
                    if (properties.isEmpty()) {
                        mostrarVazio();
                    } else {
                        adapter = new PropertyAdapter(properties, favoritados, (propertyId, agoraFavoritado) -> {
                            removerFavorito(propertyId);
                            adapter.removerItem(propertyId);
                            if (adapter.getItemCount() == 0) mostrarVazio();
                        });
                        recyclerSaved.setAdapter(adapter);
                        recyclerSaved.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    mostrarErro("Erro ao carregar imoveis: " + e.getMessage());
                });
    }

    private void removerFavorito(String propertyId) {
        SharedPreferences prefs = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        if (email.isEmpty()) return;

        DocumentReference docRef = firestore.collection("favorites").document(email);
        Map<String, Object> data = new HashMap<>();
        data.put("propertyIds", FieldValue.arrayRemove(propertyId));
        docRef.update(data)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao remover favorito", Toast.LENGTH_SHORT).show());
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
