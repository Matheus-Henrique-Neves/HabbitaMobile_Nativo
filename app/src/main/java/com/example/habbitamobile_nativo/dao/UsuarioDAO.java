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

    public UsuarioDAO(Context context) {
        con = new DatabaseConnection(context);
    }

    // Abre a conexão (deve ser chamado antes de operações)
    private void openWritable() {
        db = con.getWritableDatabase();
    }

    private void openReadable() {
        db = con.getReadableDatabase();
    }

    // Fecha a conexão
    private void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public boolean inserir(Usuario usuario) {
        if (usuario == null || usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return false;
        }

        try {
            openWritable();

            ContentValues values = new ContentValues();
            values.put("email", usuario.getEmail().trim().toLowerCase());
            values.put("senha", usuario.getSenha());

            long resultado = db.insert(DatabaseConnection.TABELA_USUARIO, null, values);
            return resultado != -1; // CORRIGIDO: retorna true se inseriu com sucesso

        } catch (Exception e) {
            e.printStackTrace();
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

            cursor = db.rawQuery(
                    "SELECT * FROM " + DatabaseConnection.TABELA_USUARIO +
                            " WHERE email=? AND senha=?",
                    new String[]{email.trim().toLowerCase(), senha}
            );

            boolean existe = cursor.getCount() > 0;
            return existe;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
    }

    // Método adicional para verificar se email já existe
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
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
    }
}
