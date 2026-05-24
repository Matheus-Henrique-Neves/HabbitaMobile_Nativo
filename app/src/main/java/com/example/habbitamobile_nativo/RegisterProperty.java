package com.example.habbitamobile_nativo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.FotoAdapter;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterProperty extends AppCompatActivity {

    private static final int MAX_FOTOS = 8;

    private TextInputEditText edtTitulo, edtEndereco, edtPreco, edtArea;
    private TextInputEditText edtQuartos, edtBanheiros, edtGaragem, edtDescricao;
    private ChipGroup chipGroupTransacao;
    private Button btnAdicionarFotos, btnCadastrar;
    private TextView txtErro;
    private RecyclerView recyclerFotos;

    private final List<Uri> fotosSelecionadas = new ArrayList<>();
    private FotoAdapter fotoAdapter;
    private Uri cameraUri;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<String> solicitarPermissaoCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) {
                    abrirCamera();
                } else {
                    mostrarErro("Permissao de camera negada. Habilite nas configuracoes do dispositivo.");
                }
            });

    private final ActivityResultLauncher<Uri> tirarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), sucesso -> {
                if (sucesso && cameraUri != null) {
                    fotosSelecionadas.add(cameraUri);
                    fotoAdapter.notifyDataSetChanged();
                }
            });

    private final ActivityResultLauncher<String[]> seletorGaleria =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris == null || uris.isEmpty()) return;

                int vagas = MAX_FOTOS - fotosSelecionadas.size();
                int limite = Math.min(uris.size(), vagas);

                for (int i = 0; i < limite; i++) {
                    Uri uri = uris.get(i);
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fotosSelecionadas.add(uri);
                }

                fotoAdapter.notifyDataSetChanged();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_property);

        initViews();
        initObjects();
        setupListeners();
    }

    private void initViews() {
        edtTitulo = findViewById(R.id.edtTitulo);
        edtEndereco = findViewById(R.id.edtEndereco);
        edtPreco = findViewById(R.id.edtPreco);
        edtArea = findViewById(R.id.edtArea);
        edtQuartos = findViewById(R.id.edtQuartos);
        edtBanheiros = findViewById(R.id.edtBanheiros);
        edtGaragem = findViewById(R.id.edtGaragem);
        edtDescricao = findViewById(R.id.edtDescricao);
        chipGroupTransacao = findViewById(R.id.chipGroupTransacao);
        btnAdicionarFotos = findViewById(R.id.btnAdicionarFotos);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        txtErro = findViewById(R.id.txtErro);
        recyclerFotos = findViewById(R.id.recyclerFotos);
    }

    private void initObjects() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        fotoAdapter = new FotoAdapter(fotosSelecionadas, posicao -> {
            fotosSelecionadas.remove(posicao);
            fotoAdapter.notifyDataSetChanged();
        });

        recyclerFotos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFotos.setAdapter(fotoAdapter);
    }

    private void setupListeners() {
        btnAdicionarFotos.setOnClickListener(v -> {
            if (fotosSelecionadas.size() >= MAX_FOTOS) {
                mostrarErro("Limite de " + MAX_FOTOS + " fotos atingido.");
                return;
            }
            mostrarDialogFonte();
        });

        btnCadastrar.setOnClickListener(v -> validarECadastrar());
    }

    private void mostrarDialogFonte() {
        new AlertDialog.Builder(this)
                .setTitle("Adicionar foto")
                .setItems(new String[]{"Tirar foto", "Escolher da galeria"}, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            abrirCamera();
                        } else {
                            solicitarPermissaoCamera.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        seletorGaleria.launch(new String[]{"image/*"});
                    }
                })
                .show();
    }

    private void abrirCamera() {
        File arquivo = new File(getCacheDir(), "foto_" + UUID.randomUUID() + ".jpg");
        cameraUri = FileProvider.getUriForFile(
                this, getPackageName() + ".fileprovider", arquivo);
        tirarFoto.launch(cameraUri);
    }

    private void validarECadastrar() {
        String titulo = getText(edtTitulo);
        String endereco = getText(edtEndereco);
        String precoStr = getText(edtPreco);
        String areaStr = getText(edtArea);
        String quartosStr = getText(edtQuartos);
        String banheirosStr = getText(edtBanheiros);
        String garagemStr = getText(edtGaragem);
        String descricao = getText(edtDescricao);

        if (titulo.isEmpty()) { mostrarErro("Informe o titulo."); return; }
        if (endereco.isEmpty()) { mostrarErro("Informe o endereco."); return; }
        if (precoStr.isEmpty()) { mostrarErro("Informe o preco."); return; }
        if (quartosStr.isEmpty()) { mostrarErro("Informe o numero de quartos."); return; }
        if (banheirosStr.isEmpty()) { mostrarErro("Informe o numero de banheiros."); return; }
        if (descricao.isEmpty()) { mostrarErro("Informe a descricao."); return; }

        double preco;
        int quartos, banheiros, garagem;
        double area;

        try {
            preco = Double.parseDouble(precoStr.replace(",", "."));
            quartos = Integer.parseInt(quartosStr);
            banheiros = Integer.parseInt(banheirosStr);
            garagem = garagemStr.isEmpty() ? 0 : Integer.parseInt(garagemStr);
            area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarErro("Valor numerico invalido.");
            return;
        }

        List<Integer> chipsSelecionados = chipGroupTransacao.getCheckedChipIds();
        String transactionType = (!chipsSelecionados.isEmpty()
                && chipsSelecionados.get(0) == R.id.chipAluguel) ? "rent" : "sell";

        txtErro.setVisibility(View.GONE);
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("Cadastrando...");

        if (fotosSelecionadas.isEmpty()) {
            salvarNoFirestore(titulo, endereco, preco, area, quartos, banheiros,
                    garagem, descricao, transactionType, new ArrayList<>());
        } else {
            uploadFotosESalvar(titulo, endereco, preco, area, quartos, banheiros,
                    garagem, descricao, transactionType);
        }
    }

    private void uploadFotosESalvar(String titulo, String endereco, double preco, double area,
                                     int quartos, int banheiros, int garagem,
                                     String descricao, String transactionType) {
        List<String> urlsFotos = new ArrayList<>();
        AtomicInteger contador = new AtomicInteger(0);
        int total = fotosSelecionadas.size();

        for (Uri uri : fotosSelecionadas) {
            String nomeArquivo = "properties/" + titulo + "/" + UUID.randomUUID() + ".jpg";
            StorageReference ref = storage.getReference().child(nomeArquivo);

            ref.putFile(uri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> {
                        urlsFotos.add(downloadUri.toString());
                        if (contador.incrementAndGet() == total) {
                            salvarNoFirestore(titulo, endereco, preco, area, quartos,
                                    banheiros, garagem, descricao, transactionType, urlsFotos);
                        }
                    })
                    .addOnFailureListener(e -> runOnUiThread(() -> {
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText("Cadastrar imovel");
                        mostrarErro("Falha ao enviar foto: " + e.getMessage());
                    }));
        }
    }

    private void salvarNoFirestore(String titulo, String endereco, double preco, double area,
                                    int quartos, int banheiros, int garagem,
                                    String descricao, String transactionType,
                                    @NonNull List<String> urlsFotos) {
        SharedPreferences prefs = getSharedPreferences("HabittaPrefs", MODE_PRIVATE);
        String emailUsuario = prefs.getString("email", "");

        Map<String, Object> imovel = new HashMap<>();
        imovel.put("title", titulo);
        imovel.put("address", endereco);
        imovel.put("price", preco);
        imovel.put("area", area);
        imovel.put("bedrooms", quartos);
        imovel.put("bathrooms", banheiros);
        imovel.put("garages", garagem);
        imovel.put("description", descricao);
        imovel.put("transactionType", transactionType);
        imovel.put("photos", urlsFotos);
        imovel.put("image_url", urlsFotos.isEmpty() ? "" : urlsFotos.get(0));
        imovel.put("owner", emailUsuario);

        firestore.collection("properties")
                .add(imovel)
                .addOnSuccessListener(ref -> runOnUiThread(() -> {
                    btnCadastrar.setEnabled(true);
                    btnCadastrar.setText("Cadastrar imovel");
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() -> {
                    btnCadastrar.setEnabled(true);
                    btnCadastrar.setText("Cadastrar imovel");
                    mostrarErro("Falha ao salvar: " + e.getMessage());
                }));
    }

    private void mostrarErro(String mensagem) {
        txtErro.setText(mensagem);
        txtErro.setVisibility(View.VISIBLE);
    }

    private String getText(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }
}
