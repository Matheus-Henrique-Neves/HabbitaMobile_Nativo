package com.example.habbitamobile_nativo;

import android.content.Intent;
import android.os.Bundle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habbitamobile_nativo.dao.UsuarioDAO;

public class MainActivity extends AppCompatActivity {

    Button btnLogin,btnTelaCadastro;
    EditText edtEmail,edtSenha;

    UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.textEmail);
        edtSenha = findViewById(R.id.textSenha);
        btnTelaCadastro = findViewById(R.id.btnCadastro);
        usuarioDAO = new UsuarioDAO(this);

        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString();
            String senha = edtSenha.getText().toString();

            if(usuarioDAO.login(email,senha)){
                abrirTelaMenu();
            }
            else{
                Toast.makeText(this,"Login Inválido",
                        Toast.LENGTH_SHORT).show();
            }

        });

        btnTelaCadastro.setOnClickListener(v -> {
            abrirTelaCadastro();
        });
    }

    public void abrirTelaCadastro(){
        Intent telaCadastro = new Intent(MainActivity.this,RegisterUser.class);
        startActivity(telaCadastro);
    }

    public void abrirTelaMenu(){
        Intent telaMenu = new Intent(MainActivity.this, Home.class);
        startActivity(telaMenu);
    }

}
