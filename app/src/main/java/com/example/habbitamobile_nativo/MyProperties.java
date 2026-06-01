package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habbitamobile_nativo.adapter.PropertyAdapter;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;

import java.util.List;

public class MyProperties extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progressBar;
    private View layoutVazio;
    private TextView txtDica;
    private PropertyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_properties);

        recycler = findViewById(R.id.recyclerMeusImoveis);
        progressBar = findViewById(R.id.progressBar);
        layoutVazio = findViewById(R.id.layoutVazio);
        txtDica = findViewById(R.id.txtDica);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    private void carregar() {
        progressBar.setVisibility(View.VISIBLE);
        layoutVazio.setVisibility(View.GONE);

        ApiService.getInstance().buscarMeusImoveis(new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties, boolean hasMore) {
                runOnUiThread(() -> exibir(properties));
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    layoutVazio.setVisibility(View.VISIBLE);
                    Toast.makeText(MyProperties.this, "Erro: " + mensagem, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void exibir(List<Property> properties) {
        progressBar.setVisibility(View.GONE);

        if (properties.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtDica.setVisibility(View.GONE);
            layoutVazio.setVisibility(View.VISIBLE);
            return;
        }

        layoutVazio.setVisibility(View.GONE);
        txtDica.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.VISIBLE);

        adapter = new PropertyAdapter(properties);
        adapter.setOnPropertyClickListener(p -> {
            Intent intent = new Intent(MyProperties.this, PropertyDetails.class);
            intent.putExtra(PropertyDetails.EXTRA_PROPERTY, p);
            startActivity(intent);
        });
        adapter.setOnPropertyLongClickListener(this::confirmarExclusao);
        recycler.setAdapter(adapter);
    }

    private void confirmarExclusao(Property p) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir imovel")
                .setMessage("Deseja excluir \"" + p.getTitle() + "\"? Esta acao nao pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> excluir(p))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluir(Property p) {
        ApiService.getInstance().excluirImovel(p.getId(), new ApiService.SimpleCallback() {
            @Override
            public void onSucesso() {
                runOnUiThread(() -> {
                    if (adapter != null) adapter.removerItem(p.getId());
                    Toast.makeText(MyProperties.this, "Imovel excluido", Toast.LENGTH_SHORT).show();
                    if (adapter != null && adapter.getItemCount() == 0) {
                        recycler.setVisibility(View.GONE);
                        txtDica.setVisibility(View.GONE);
                        layoutVazio.setVisibility(View.VISIBLE);
                    }
                });
            }
            @Override
            public void onFalha(String mensagem) {
                runOnUiThread(() ->
                        Toast.makeText(MyProperties.this, "Erro ao excluir: " + mensagem, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
