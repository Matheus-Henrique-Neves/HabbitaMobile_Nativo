package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.model.Property;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends BaseActivity {

    private RecyclerView recyclerImoveis;
    private ProgressBar progressBar;
    private TextView txtErro;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_home, findViewById(R.id.container));

        initViews();
        initObjects();
        setSelectedNavItem(R.id.navigation_home);
        carregarImoveis();
    }

    private void initViews() {
        recyclerImoveis = findViewById(R.id.recyclerImoveis);
        progressBar = findViewById(R.id.progressBar);
        txtErro = findViewById(R.id.txtErro);
        recyclerImoveis.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initObjects() {
        firestore = FirebaseFirestore.getInstance();
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
