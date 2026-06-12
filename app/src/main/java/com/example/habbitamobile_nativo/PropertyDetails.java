package com.example.habbitamobile_nativo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.api.ApiService;
import com.example.habbitamobile_nativo.model.Property;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PropertyDetails extends AppCompatActivity {

    public static final String EXTRA_PROPERTY = "property";

    private Property property;
    private boolean primeiraExibicao = true;
    private boolean favoritado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        property = (Property) getIntent().getSerializableExtra(EXTRA_PROPERTY);
        if (property == null) {
            finish();
            return;
        }

        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> onBackPressed());

        configurarFavorito();
        exibir();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (primeiraExibicao) {
            primeiraExibicao = false;
            return;
        }
        recarregar();
    }

    private void recarregar() {
        if (property == null || property.getId() == null) return;
        ApiService.getInstance().buscarImovelPorId(property.getId(), new ApiService.BuscarImoveisCallback() {
            @Override
            public void onSucesso(List<Property> properties, boolean hasMore) {
                if (!properties.isEmpty()) {
                    runOnUiThread(() -> {
                        property = properties.get(0);
                        exibir();
                    });
                }
            }
            @Override
            public void onFalha(String mensagem) {}
        });
    }

    private void exibir() {
        preencherDados(property);
        configurarContato(property);
        configurarEdicao(property);
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
        if (isAluguel) {
            chipTipo.getBackground().mutate().setTint(Color.parseColor("#BBDEFB"));
            chipTipo.setTextColor(Color.parseColor("#0D47A1"));
        } else {
            chipTipo.getBackground().mutate().setTint(Color.parseColor("#C8E6C9"));
            chipTipo.setTextColor(Color.parseColor("#1B5E20"));
        }

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

    private void configurarFavorito() {
        ImageButton btnFavoritar = findViewById(R.id.btnFavoritarDetalhe);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            btnFavoritar.setVisibility(View.GONE);
            return;
        }
        btnFavoritar.setVisibility(View.VISIBLE);

        ApiService.getInstance().buscarFavoritos(new ApiService.BuscarFavoritosCallback() {
            @Override
            public void onSucesso(List<String> ids) {
                favoritado = property != null && ids.contains(property.getId());
                runOnUiThread(() -> atualizarIconeFavorito(btnFavoritar));
            }
            @Override
            public void onFalha(String mensagem) {}
        });

        btnFavoritar.setOnClickListener(v -> {
            if (property == null || property.getId() == null) return;
            boolean novoEstado = !favoritado;
            favoritado = novoEstado;
            atualizarIconeFavorito(btnFavoritar);

            ApiService.SimpleCallback callback = new ApiService.SimpleCallback() {
                @Override
                public void onSucesso() {}
                @Override
                public void onFalha(String mensagem) {
                    runOnUiThread(() -> {
                        favoritado = !novoEstado;
                        atualizarIconeFavorito(btnFavoritar);
                        Toast.makeText(PropertyDetails.this,
                                "Nao foi possivel atualizar favoritos", Toast.LENGTH_SHORT).show();
                    });
                }
            };

            if (novoEstado) {
                ApiService.getInstance().adicionarFavorito(property.getId(), callback);
            } else {
                ApiService.getInstance().removerFavorito(property.getId(), callback);
            }
        });
    }

    private void atualizarIconeFavorito(ImageButton btn) {
        btn.setImageResource(favoritado ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);
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
            btnWhatsapp.setVisibility(View.VISIBLE);
            btnWhatsapp.setOnClickListener(v -> abrirWhatsapp(numeroLimpo, titulo));
            btnLigar.setVisibility(View.VISIBLE);
            btnLigar.setOnClickListener(v -> ligar(telefone));
        } else {
            btnWhatsapp.setVisibility(View.GONE);
            btnLigar.setVisibility(View.GONE);
        }

        if (email != null && !email.isEmpty()) {
            btnEmail.setVisibility(View.VISIBLE);
            btnEmail.setOnClickListener(v -> enviarEmail(email, titulo));
        } else {
            btnEmail.setVisibility(View.GONE);
        }
    }

    private void configurarEdicao(Property p) {
        ImageButton btnEditar = findViewById(R.id.btnEditar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        boolean ehDono = user != null && p.getOwner() != null
                && (p.getOwner().equalsIgnoreCase(user.getEmail()) || p.getOwner().equals(user.getUid()));

        if (ehDono) {
            btnEditar.setVisibility(View.VISIBLE);
            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(PropertyDetails.this, RegisterProperty.class);
                intent.putExtra(RegisterProperty.EXTRA_PROPERTY_EDIT, p);
                startActivity(intent);
            });
        } else {
            btnEditar.setVisibility(View.GONE);
        }
    }

    private void abrirWhatsapp(String numero, String titulo) {
        try {
            String mensagem = "Ola! Tenho interesse no imovel: " + titulo;
            String url = "https://wa.me/" + numero + "?text=" + Uri.encode(mensagem);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nao foi possivel abrir o WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void ligar(String telefone) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefone)));
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
