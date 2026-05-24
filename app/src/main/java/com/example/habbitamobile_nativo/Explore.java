package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Explore extends BaseActivity {

    private static final String URL_PROPERTIES = "https://habitta-mobile.onrender.com/properties";

    private RecyclerView recyclerExplore;
    private ProgressBar progressBar;
    private TextView txtErro;
    private TextInputEditText edtBusca;
    private ChipGroup chipGroup;
    private OkHttpClient httpClient;

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
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

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

        Request request = new Request.Builder()
                .url(URL_PROPERTIES)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    txtErro.setVisibility(View.VISIBLE);
                    txtErro.setText("Sem conexao. Verifique sua internet.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        txtErro.setVisibility(View.VISIBLE);
                        txtErro.setText("Erro ao buscar imoveis: " + response.code());
                    });
                    return;
                }

                String json = response.body().string();

                try {
                    List<Property> properties = parsearImoveis(json);
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        todasPropriedades.clear();
                        todasPropriedades.addAll(properties);
                        filtrarEAtualizar();
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        txtErro.setVisibility(View.VISIBLE);
                        txtErro.setText("Erro ao processar dados recebidos.");
                    });
                }
            }
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

    private List<Property> parsearImoveis(String json) throws JSONException {
        List<Property> lista = new ArrayList<>();
        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Property p = new Property();
            p.setId(obj.optString("id"));
            p.setImageUrl(obj.optString("image_url"));
            p.setTitle(obj.optString("title"));
            p.setPrice(obj.optDouble("price", 0));
            p.setBedrooms(obj.optInt("bedrooms", 0));
            p.setBathrooms(obj.optInt("bathrooms", 0));
            p.setGarages(obj.optInt("garages", 0));
            p.setAddress(obj.optString("address"));
            p.setDescription(obj.optString("description"));
            p.setType(obj.optString("type"));
            p.setTransactionType(obj.optString("transactionType"));
            p.setArea(obj.optDouble("area", 0));
            lista.add(p);
        }

        return lista;
    }
}
