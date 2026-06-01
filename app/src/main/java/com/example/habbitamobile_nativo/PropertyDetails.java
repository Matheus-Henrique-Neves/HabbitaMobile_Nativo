package com.example.habbitamobile_nativo;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.model.Property;

import java.text.NumberFormat;
import java.util.Locale;

public class PropertyDetails extends AppCompatActivity {

    public static final String EXTRA_PROPERTY = "property";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        Property property = (Property) getIntent().getSerializableExtra(EXTRA_PROPERTY);
        if (property == null) {
            finish();
            return;
        }

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        preencherDados(property);
    }

    private void preencherDados(Property p) {
        ImageView imgDetalhes = findViewById(R.id.imgDetalhes);
        TextView chipTipo = findViewById(R.id.chipTipoTransacao);
        TextView txtTitulo = findViewById(R.id.txtDetalheTitulo);
        TextView txtPreco = findViewById(R.id.txtDetalhePreco);
        TextView txtQuartos = findViewById(R.id.txtDetalheQuartos);
        TextView txtBanheiros = findViewById(R.id.txtDetalheBanheiros);
        TextView txtVagas = findViewById(R.id.txtDetalheVagas);
        TextView txtArea = findViewById(R.id.txtDetalheArea);
        TextView txtEndereco = findViewById(R.id.txtDetalheEndereco);
        TextView txtDescricao = findViewById(R.id.txtDetalheDescricao);

        String fotoUrl = p.getImageUrl();
        if ((fotoUrl == null || fotoUrl.isEmpty()) && p.getPhotos() != null && !p.getPhotos().isEmpty()) {
            fotoUrl = p.getPhotos().get(0);
        }

        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            Glide.with(this).load(fotoUrl).centerCrop().into(imgDetalhes);
        } else {
            imgDetalhes.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        boolean isAluguel = "rent".equalsIgnoreCase(p.getTransactionType());
        chipTipo.setText(isAluguel ? "Aluguel" : "Venda");

        txtTitulo.setText(p.getTitle() != null ? p.getTitle() : "");

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        String precoFormatado = "R$ " + nf.format((long) p.getPrice());
        if (isAluguel) precoFormatado += "/mes";
        txtPreco.setText(precoFormatado);

        txtQuartos.setText(String.valueOf(p.getBedrooms()));
        txtBanheiros.setText(String.valueOf(p.getBathrooms()));
        txtVagas.setText(String.valueOf(p.getGarages()));
        txtArea.setText(p.getArea() > 0 ? String.valueOf((int) p.getArea()) : "-");

        txtEndereco.setText(p.getAddress() != null ? p.getAddress() : "-");
        txtDescricao.setText(p.getDescription() != null && !p.getDescription().isEmpty()
                ? p.getDescription() : "Sem descricao.");
    }
}
