package com.emsi.tp4_sqlite;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emsi.tp4_sqlite.adapter.EtudiantAdapter;
import com.emsi.tp4_sqlite.classes.Etudiant;
import com.emsi.tp4_sqlite.service.EtudiantService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText nom, prenom, dateNaissance, id;
    private Button add, rechercher, delete, btnListe, btnPhoto;
    private TextView res;
    private ImageView ivStudentPhoto;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSION_CODE = 101;
    private ImageView currentEditDialogImageView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private byte[] currentPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermissions();
        setupListeners();
    }

    private void initViews() {
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        dateNaissance = findViewById(R.id.dateNaissance);
        id = findViewById(R.id.id);
        add = findViewById(R.id.bn);
        rechercher = findViewById(R.id.load);
        delete = findViewById(R.id.delete);
        btnListe = findViewById(R.id.btnListe);
        btnPhoto = findViewById(R.id.btnPhoto);
        ivStudentPhoto = findViewById(R.id.ivStudentPhoto);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void setupListeners() {
        dateNaissance.setOnClickListener(v -> showDatePickerDialog());
        btnPhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        add.setOnClickListener(v -> addEtudiant());
        rechercher.setOnClickListener(v -> searchEtudiant());
        delete.setOnClickListener(v -> deleteEtudiant());
        btnListe.setOnClickListener(v -> startActivity(new Intent(this, ListActivityEtudiant.class)));

        btnListe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListActivityEtudiant.class);
                startActivity(intent);
            }
        });
    }

    private void addEtudiant() {
        if (validateFields()) {
            EtudiantService es = new EtudiantService(this);
            Date date = parseDate(dateNaissance.getText().toString());

            Etudiant etudiant = new Etudiant(
                    nom.getText().toString(),
                    prenom.getText().toString(),
                    date
            );

            if (currentPhoto != null) {
                etudiant.setPhoto(currentPhoto);
            }

            es.create(etudiant);
            clearFields();
            Toast.makeText(this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchEtudiant() {
        if (id.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int studentId = Integer.parseInt(id.getText().toString());
        EtudiantService es = new EtudiantService(this);
        Etudiant e = es.findById(studentId);

        if (e != null) {
            nom.setText(e.getNom());
            prenom.setText(e.getPrenom());
            dateNaissance.setText(dateFormat.format(e.getDateNaissance()));

            if (e.getPhoto() != null) {
                ivStudentPhoto.setImageBitmap(byteArrayToBitmap(e.getPhoto()));
                currentPhoto = e.getPhoto();
            } else {
                ivStudentPhoto.setImageResource(R.drawable.ic_person_ouline);
            }
        } else {
            Toast.makeText(this, "Aucun étudiant trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteEtudiant() {
        if (id.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int studentId = Integer.parseInt(id.getText().toString());
        EtudiantService es = new EtudiantService(this);
        Etudiant e = es.findById(studentId);

        if (e != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Voulez-vous vraiment supprimer " + e.getNom() + "?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        es.delete(e);
                        clearFields();
                        Toast.makeText(this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        } else {
            Toast.makeText(this, "Aucun étudiant trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptionsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Modifier la photo")
                .setItems(new CharSequence[]{"Prendre une photo", "Choisir depuis la galerie"},
                        (dialog, which) -> {
                            if (which == 0) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchPickPictureIntent();
                            }
                        })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                ivStudentPhoto.setImageBitmap(bitmap);
                currentPhoto = bitmapToByteArray(bitmap);
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap byteArrayToBitmap(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private void clearFields() {
        nom.setText("");
        prenom.setText("");
        dateNaissance.setText("");
        id.setText("");
        currentPhoto = null;
        ivStudentPhoto.setImageResource(R.drawable.ic_person_ouline);
    }

    private boolean validateFields() {
        if (nom.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (prenom.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un prénom", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dateNaissance.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner une date de naissance", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    dateNaissance.setText(dateFormat.format(selectedDate.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private Date parseDate(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCurrentEditDialogImageView(ImageView imageView) {
        currentEditDialogImageView = imageView;
    }

    public ImageView getCurrentEditDialogImageView() {
        return currentEditDialogImageView;
    }
}
