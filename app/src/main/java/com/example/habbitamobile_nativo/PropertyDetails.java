package com.example.habbitamobile_nativo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.button.MaterialButton;

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
        configurarContato(property);
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

    private void configurarContato(Property p) {
        MaterialButton btnWhatsapp = findViewById(R.id.btnWhatsapp);
        MaterialButton btnLigar = findViewById(R.id.btnLigar);
        MaterialButton btnEmail = findViewById(R.id.btnEmailContato);

        String telefone = p.getContactPhone();
        String email = p.getContactEmail();
        String titulo = p.getTitle() != null ? p.getTitle() : "imovel";

        if (telefone != null && !telefone.isEmpty()) {
            String numeroLimpo = telefone.replaceAll("[^0-9]", "");

            btnWhatsapp.setVisibility(android.view.View.VISIBLE);
            btnWhatsapp.setOnClickListener(v -> abrirWhatsapp(numeroLimpo, titulo));

            btnLigar.setVisibility(android.view.View.VISIBLE);
            btnLigar.setOnClickListener(v -> ligar(telefone));
        }

        if (email != null && !email.isEmpty()) {
            btnEmail.setVisibility(android.view.View.VISIBLE);
            btnEmail.setOnClickListener(v -> enviarEmail(email, titulo));
        }
    }

    private void abrirWhatsapp(String numero, String titulo) {
        try {
            String mensagem = "Ola! Tenho interesse no imovel: " + titulo;
            String url = "https://wa.me/" + numero + "?text=" + Uri.encode(mensagem);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nao foi possivel abrir o WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void ligar(String telefone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefone));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nao foi possivel abrir o discador", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarEmail(String email, String titulo) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Interesse no imovel: " + titulo);
            intent.putExtra(Intent.EXTRA_TEXT, "Ola! Tenho interesse no imovel anunciado.");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nenhum app de email encontrado", Toast.LENGTH_SHORT).show();
        }
    }
}
