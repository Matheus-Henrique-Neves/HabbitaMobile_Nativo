package com.example.habbitamobile_nativo.api;

import com.example.habbitamobile_nativo.model.Property;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private static final String BASE_URL = "https://habitta-api-agf3.onrender.com";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static ApiService instance;
    private final OkHttpClient client;

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public interface BuscarImoveisCallback {
        void onSucesso(List<Property> properties, boolean hasMore);
        void onFalha(String mensagem);
    }

    public interface BuscarFavoritosCallback {
        void onSucesso(List<String> propertyIds);
        void onFalha(String mensagem);
    }

    public interface CadastrarCallback {
        void onSucesso(String id);
        void onFalha(String mensagem);
    }

    public interface SimpleCallback {
        void onSucesso();
        void onFalha(String mensagem);
    }

    private void getToken(TokenCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onToken(null);
            return;
        }
        user.getIdToken(false)
                .addOnSuccessListener(result -> callback.onToken(result.getToken()))
                .addOnFailureListener(e -> callback.onToken(null));
    }

    private interface TokenCallback {
        void onToken(String token);
    }

    public void buscarImoveis(int page, BuscarImoveisCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/properties?page=" + page + "&limit=10")
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
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray arr = obj.getJSONArray("properties");
                    boolean hasMore = obj.optBoolean("hasMore", false);
                    List<Property> properties = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        properties.add(parsearImovel(arr.getJSONObject(i)));
                    }
                    callback.onSucesso(properties, hasMore);
                } catch (JSONException e) {
                    callback.onFalha("Erro ao processar resposta");
                }
            }
        });
    }

    public void buscarImovelPorId(String id, BuscarImoveisCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/properties/" + id)
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
                try {
                    List<Property> lista = new ArrayList<>();
                    lista.add(parsearImovel(new JSONObject(response.body().string())));
                    callback.onSucesso(lista, false);
                } catch (JSONException e) {
                    callback.onFalha("Erro ao processar resposta");
                }
            }
        });
    }

    public void cadastrarImovel(JSONObject dados, CadastrarCallback callback) {
        getToken(token -> {
            if (token == null) {
                callback.onFalha("Usuario nao autenticado");
                return;
            }
            RequestBody body = RequestBody.create(dados.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/properties")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
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
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        callback.onSucesso(obj.optString("_id", ""));
                    } catch (JSONException e) {
                        callback.onFalha("Erro ao processar resposta");
                    }
                }
            });
        });
    }

    public void buscarFavoritos(BuscarFavoritosCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFalha("Usuario nao autenticado");
            return;
        }
        String uid = user.getUid();
        getToken(token -> {
            if (token == null) {
                callback.onFalha("Token invalido");
                return;
            }
            Request request = new Request.Builder()
                    .url(BASE_URL + "/favorites/" + uid)
                    .addHeader("Authorization", "Bearer " + token)
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
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        JSONArray arr = obj.optJSONArray("propertyIds");
                        List<String> ids = new ArrayList<>();
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                ids.add(arr.getString(i));
                            }
                        }
                        callback.onSucesso(ids);
                    } catch (JSONException e) {
                        callback.onFalha("Erro ao processar resposta");
                    }
                }
            });
        });
    }

    public void adicionarFavorito(String propertyId, SimpleCallback callback) {
        enviarFavorito("/favorites/add", propertyId, callback);
    }

    public void removerFavorito(String propertyId, SimpleCallback callback) {
        enviarFavorito("/favorites/remove", propertyId, callback);
    }

    private void enviarFavorito(String rota, String propertyId, SimpleCallback callback) {
        getToken(token -> {
            if (token == null) {
                callback.onFalha("Usuario nao autenticado");
                return;
            }
            try {
                JSONObject json = new JSONObject();
                json.put("propertyId", propertyId);
                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(BASE_URL + rota)
                        .addHeader("Authorization", "Bearer " + token)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onFalha(e.getMessage() != null ? e.getMessage() : "Erro de conexao");
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        if (response.isSuccessful()) {
                            callback.onSucesso();
                        } else {
                            callback.onFalha("Erro " + response.code());
                        }
                    }
                });
            } catch (JSONException e) {
                callback.onFalha("Erro ao montar requisicao");
            }
        });
    }

    private List<Property> parsearImoveis(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        List<Property> properties = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            properties.add(parsearImovel(array.getJSONObject(i)));
        }
        return properties;
    }

    private Property parsearImovel(JSONObject obj) throws JSONException {
        Property p = new Property();
        p.setId(obj.optString("_id", obj.optString("id", "")));
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
        p.setContactEmail(obj.optString("contactEmail", ""));
        p.setContactPhone(obj.optString("contactPhone", ""));

        JSONArray photosArr = obj.optJSONArray("photos");
        if (photosArr != null) {
            List<String> photos = new ArrayList<>();
            for (int i = 0; i < photosArr.length(); i++) photos.add(photosArr.getString(i));
            p.setPhotos(photos);
        }
        return p;
    }
}
