package com.example.habbitamobile_nativo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habbitamobile_nativo.database.DatabaseConnection;
import com.example.habbitamobile_nativo.model.Usuario;
import com.example.habbitamobile_nativo.util.SenhaUtil;

public class UsuarioDAO {

    private SQLiteDatabase db;
    private final DatabaseConnection con;

    public UsuarioDAO(Context context) {
        con = new DatabaseConnection(context);
    }

    private void openWritable() {
        db = con.getWritableDatabase();
    }

    private void openReadable() {
        db = con.getReadableDatabase();
    }

    private void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public boolean inserir(Usuario usuario) {
        if (usuario == null || !usuario.isValid()) {
            return false;
        }

        try {
            openWritable();
            ContentValues values = new ContentValues();
            values.put("nome", usuario.getNome());
            values.put("email", usuario.getEmail());
            values.put("senha", usuario.getSenha());
            long resultado = db.insert(DatabaseConnection.TABELA_USUARIO, null, values);
            return resultado != -1;
        } catch (Exception e) {
            return false;
        } finally {
            close();
        }
    }

    public boolean login(String email, String senha) {
        if (email == null || email.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            return false;
        }

        Cursor cursor = null;
        try {
            openReadable();
            String senhaHash = SenhaUtil.hash(senha);
            cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseConnection.TABELA_USUARIO +
                            " WHERE email=? AND senha=?",
                    new String[]{email.trim().toLowerCase(), senhaHash}
            );
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) cursor.close();
            close();
        }
    }

    public String buscarNome(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }

        Cursor cursor = null;
        try {
            openReadable();
            cursor = db.rawQuery(
                    "SELECT nome FROM " + DatabaseConnection.TABELA_USUARIO +
                            " WHERE email=?",
                    new String[]{email.trim().toLowerCase()}
            );
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
            return "";
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) cursor.close();
            close();
        }
    }

    public boolean emailExiste(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Cursor cursor = null;
        try {
            openReadable();
            cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseConnection.TABELA_USUARIO +
                            " WHERE email=?",
                    new String[]{email.trim().toLowerCase()}
            );
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) cursor.close();
            close();
        }
    }
}
