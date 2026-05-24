package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Explore extends BaseActivity {

    private RecyclerView recyclerExplore;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextInputEditText edtBusca;
    private ChipGroup chipGroup;
    private FirebaseFirestore firestore;

    private final List<Property> todasPropriedades = new ArrayList<>();
    private String filtroTipo = "todos";
    private String termoBusca = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_explore, findViewById(R.id.container));

        initViews();
        initObjects();
        setSelectedNavItem(R.id.navigation_explore);
        carregarImoveis();
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                termoBusca = s.toString().toLowerCase().trim();
                filtrarEAtualizar();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

    private void carregarImoveis() {
        progressBar.setVisibility(View.VISIBLE);
        txtErro.setVisibility(View.GONE);

        firestore.collection("properties")
                .get()
                .addOnSuccessListener(snapshot -> {
                    todasPropriedades.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        todasPropriedades.add(documentParaProperty(doc));
                    }
                    progressBar.setVisibility(View.GONE);
                    filtrarEAtualizar();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    txtErro.setVisibility(View.VISIBLE);
                    txtErro.setText("Erro ao carregar imoveis: " + e.getMessage());
                });
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
        } else {
            txtErro.setVisibility(View.GONE);
            recyclerExplore.setAdapter(new PropertyAdapter(filtradas));
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
