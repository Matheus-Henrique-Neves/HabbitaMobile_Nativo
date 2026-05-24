package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.model.Property;

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

public class Home extends BaseActivity {

    private static final String URL_PROPERTIES = "https://habitta-mobile.onrender.com/properties";

    private RecyclerView recyclerImoveis;
    private ProgressBar progressBar;
    private TextView txtErro;
    private OkHttpClient httpClient;

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
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
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
                        if (properties.isEmpty()) {
                            txtErro.setVisibility(View.VISIBLE);
                            txtErro.setText("Nenhum imovel encontrado.");
                        } else {
                            recyclerImoveis.setAdapter(new PropertyAdapter(properties));
                        }
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
