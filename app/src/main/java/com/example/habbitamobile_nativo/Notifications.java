package com.example.habbitamobile_nativo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class Notifications extends AppCompatActivity {

    private static final String PREFS = "HabittaPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        SwitchCompat switchOfertas = findViewById(R.id.switchOfertas);
        SwitchCompat switchNovos = findViewById(R.id.switchNovos);
        SwitchCompat switchAtualizacoes = findViewById(R.id.switchAtualizacoes);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        switchOfertas.setChecked(prefs.getBoolean("notif_ofertas", true));
        switchNovos.setChecked(prefs.getBoolean("notif_novos", true));
        switchAtualizacoes.setChecked(prefs.getBoolean("notif_atualizacoes", false));

        switchOfertas.setOnCheckedChangeListener((b, marcado) ->
                prefs.edit().putBoolean("notif_ofertas", marcado).apply());
        switchNovos.setOnCheckedChangeListener((b, marcado) ->
                prefs.edit().putBoolean("notif_novos", marcado).apply());
        switchAtualizacoes.setOnCheckedChangeListener((b, marcado) ->
                prefs.edit().putBoolean("notif_atualizacoes", marcado).apply());

        btnVoltar.setOnClickListener(v -> finish());
    }
}
