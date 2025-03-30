package com.emsi.tp4_sqlite.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2; // Incrémenté pour la nouvelle colonne
    private static final String DATABASE_NAME = "ecole.db";

    // Définition de la table etudiant
    private static final String CREATE_TABLE_ETUDIANT = "CREATE TABLE etudiant(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "prenom TEXT NOT NULL," +
            "nom TEXT NOT NULL," +
            "photo BLOB," +  // Colonne pour stocker les images
            "date_naissance INTEGER)";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crée la table lors de la première installation
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Stratégie de migration progressive
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN photo BLOB");
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS etudiant");
        onCreate(db);
    }
}