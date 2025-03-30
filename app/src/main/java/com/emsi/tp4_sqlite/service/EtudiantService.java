package com.emsi.tp4_sqlite.service;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.emsi.tp4_sqlite.classes.Etudiant;
import com.emsi.tp4_sqlite.util.MySQLiteHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtudiantService {
    private static final String TABLE_NAME = "etudiant";

    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance"; // New column
    private static final String KEY_PHOTO = "photo";

    private static String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE_NAISSANCE, KEY_PHOTO};
    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance() != null ? e.getDateNaissance().getTime() : 0);
        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto());
        }
        db.insert(TABLE_NAME, null, values);
        Log.d("insert", e.getNom());
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance() != null ? e.getDateNaissance().getTime() : 0);
        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto());
        }
        db.update(TABLE_NAME,
                values,
                "id = ?",
                new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id) {
        Etudiant e = null;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c;
        c = db.query(TABLE_NAME,
                COLUMNS,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null);
        if (c.moveToFirst()) {
            e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));
            e.setDateNaissance(new Date(c.getLong(3)));
            e.setPhoto(c.getBlob(4));
        }
        db.close();
        return e;
    }


    public void delete(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                KEY_ID + " = ?",
                new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public List<Etudiant> findAll() {
        List<Etudiant> etudiants = new ArrayList<>();
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM etudiant", null);

        while (cursor.moveToNext()) {
            Etudiant e = new Etudiant();
            e.setId(cursor.getInt(0)); // id
            e.setPrenom(cursor.getString(1)); // prenom
            e.setNom(cursor.getString(2)); // nom

            // Handle BLOB photo (DO NOT convert to long)
            byte[] photoBytes = cursor.getBlob(3); // photo column
            if (photoBytes != null) {
                e.setPhoto(photoBytes);
            }

            // Handle DATE (not long)
            String dateString = cursor.getString(4); // date_naissance as String (e.g., "2025-03-30")
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(dateString);
                e.setDateNaissance(date);
            } catch (ParseException ex) {
                ex.printStackTrace();
                e.setDateNaissance(new Date()); // Fallback to current date
            }

            etudiants.add(e);
        }
        cursor.close();
        return etudiants;
    }
}