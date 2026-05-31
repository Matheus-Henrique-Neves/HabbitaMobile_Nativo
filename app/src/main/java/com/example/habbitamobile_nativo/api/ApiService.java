package com.example.habbitamobile_nativo.api;

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

public class ApiService {

    private static final String BASE_URL = "https://habitta-mobile.onrender.com";
    private static ApiService instance;
    private final OkHttpClient client;

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public interface BuscarImoveisCallback {
        void onSucesso(List<Property> properties);
        void onFalha(String mensagem);
    }

    public void buscarImoveis(BuscarImoveisCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/properties")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFalha(e.getMessage() != null ? e.getMessage() : "Erro de conexao");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFalha("Erro " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    List<Property> properties = parsearImoveis(body);
                    callback.onSucesso(properties);
                } catch (JSONException e) {
                    callback.onFalha("Erro ao processar resposta da API");
                }
            }
        });
    }

    private List<Property> parsearImoveis(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        List<Property> properties = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Property p = new Property();
            p.setId("api_" + obj.optInt("id", i));
            p.setTitle(obj.optString("title", ""));
            p.setImageUrl(obj.optString("image_url", ""));
            p.setAddress(obj.optString("address", ""));
            p.setDescription(obj.optString("description", ""));
            p.setType(obj.optString("type", ""));
            p.setTransactionType(obj.optString("transactionType", ""));
            p.setPrice(obj.optDouble("price", 0));
            p.setArea(obj.optDouble("area", 0));
            p.setBedrooms(obj.optInt("bedrooms", 0));
            p.setBathrooms(obj.optInt("bathrooms", 0));
            p.setGarages(obj.optInt("garages", 0));
            properties.add(p);
        }

        return properties;
    }
}
