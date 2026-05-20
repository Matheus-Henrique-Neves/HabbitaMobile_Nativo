package com.example.habbitamobile_nativo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseConnection extends SQLiteOpenHelper {

    private static final String DB_NAME = "app_login.db"; // Adicionada extensão .db
    private static final int DB_VERSION = 2; // Versão incrementada
    public static final String TABELA_USUARIO = "usuarios";

    public DatabaseConnection(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_USUARIO + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," + // NOT NULL adicionado
                "senha TEXT NOT NULL," + // NOT NULL adicionado
                "data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP)"; // Timestamp adicionado

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABELA_USUARIO);
            onCreate(db);
        }
    }
}