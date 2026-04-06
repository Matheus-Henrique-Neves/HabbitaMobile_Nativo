package com.example.habbitamobile_nativo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habbitamobile_nativo.database.DatabaseConnection;
import com.example.habbitamobile_nativo.model.Usuario;

public class UsuarioDAO {

    private SQLiteDatabase db;
    private DatabaseConnection con;

    public UsuarioDAO(Context context){
        con = new DatabaseConnection(context);
    }

    public boolean inserir(Usuario usuario){

        db = con.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("email",usuario.getEmail());
        values.put("senha",usuario.getSenha());

        long resultado = db.insert(DatabaseConnection.TABELA_USUARIO, null,values);

        return resultado != 1;

    }

    public boolean login(String email, String senha){
        db = con.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM "+DatabaseConnection.TABELA_USUARIO+
                        " WHERE email=? AND senha=?",
                new String[]{email, senha}
        );
        boolean existe = cursor.getCount()>0;
        return  existe;




    }

}
