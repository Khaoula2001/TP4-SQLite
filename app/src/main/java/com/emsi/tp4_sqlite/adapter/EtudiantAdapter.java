package com.emsi.tp4_sqlite.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.tp4_sqlite.MainActivity;
import com.emsi.tp4_sqlite.R;
import com.emsi.tp4_sqlite.classes.Etudiant;
import com.emsi.tp4_sqlite.service.EtudiantService;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {
    private Context context;
    private List<Etudiant> etudiantList;
    private EtudiantService etudiantService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private byte[] data;

    public EtudiantAdapter(Context context, List<Etudiant> etudiantList) {
        this.context = context;
        this.etudiantList = etudiantList;
        this.etudiantService = new EtudiantService(context);
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiantList.get(position);

        holder.tvId.setText("ID: " + etudiant.getId());
        holder.tvNom.setText(etudiant.getNom());
        holder.tvPrenom.setText(etudiant.getPrenom());
        holder.tvDateNaissance.setText(dateFormat.format(etudiant.getDateNaissance()));

        if (etudiant.getPhoto() != null && etudiant.getPhoto().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(etudiant.getPhoto(), 0, etudiant.getPhoto().length);
            holder.ivPhoto.setImageBitmap(bitmap);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_person_ouline);
        }

        holder.itemView.setOnClickListener(v -> showOptionsDialog(etudiant, position));
    }

    @Override
    public int getItemCount() {
        return etudiantList.size();
    }

    private void showOptionsDialog(Etudiant etudiant, int position) {
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle("Options")
                .setItems(new CharSequence[]{"Modifier", "Supprimer"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(etudiant, position);
                    } else {
                        showDeleteConfirmationDialog(etudiant, position);
                    }
                })
                .show();
    }

    private void showEditDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.edit_etudiant, null);
        builder.setView(dialogView);

        // Initialiser les vues
        EditText etNom = dialogView.findViewById(R.id.etNom);
        EditText etPrenom = dialogView.findViewById(R.id.etPrenom);
        EditText etDateNaissance = dialogView.findViewById(R.id.etDateNaissance);
        ImageView ivPhoto = dialogView.findViewById(R.id.ivPhoto);
        Button btnChangePhoto = dialogView.findViewById(R.id.btnChangePhoto);


        if (context instanceof MainActivity) {
            ((MainActivity) context).setCurrentEditDialogImageView(ivPhoto);
        }


        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());
        etDateNaissance.setText(dateFormat.format(etudiant.getDateNaissance()));


        etDateNaissance.setOnClickListener(v -> showDatePicker(etDateNaissance, etudiant));

        btnChangePhoto.setOnClickListener(v -> showPhotoOptionsDialog(ivPhoto));
        data = etudiant.getPhoto();
        if (data != null && data.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ivPhoto.setImageBitmap(bitmap);
        } else {
            ivPhoto.setImageResource(R.mipmap.ic_launcher);
        }


        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            etudiant.setNom(etNom.getText().toString());
            etudiant.setPrenom(etPrenom.getText().toString());

            // Convertir l'image affichée en byte[]
            ivPhoto.setDrawingCacheEnabled(true);
            ivPhoto.buildDrawingCache();
            Bitmap bitmap = ivPhoto.getDrawingCache();
            byte[] imageBytes = bitmapToByteArray(bitmap);

            etudiant.setPhoto(imageBytes);
            etudiantService.create(etudiant);
            notifyItemChanged(position);
            Toast.makeText(context, "Étudiant modifié", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void showDatePicker(EditText etDateNaissance, Etudiant etudiant) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(etudiant.getDateNaissance());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Sélectionnez la date de naissance")
                .setSelection(calendar.getTimeInMillis())
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            etDateNaissance.setText(dateFormat.format(selectedDate.getTime()));
            etudiant.setDateNaissance(selectedDate.getTime());
        });

        if (context instanceof FragmentActivity) {
            datePicker.show(((FragmentActivity) context).getSupportFragmentManager(), "DATE_PICKER");
        } else {
            Toast.makeText(context, "Erreur: Impossible d'afficher le sélecteur de date", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptionsDialog(ImageView imageView) {
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle("Changer la photo")
                .setItems(new CharSequence[]{"Prendre une photo", "Choisir depuis la galerie"}, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent(imageView);
                    } else {
                        dispatchPickPictureIntent(imageView);
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent(ImageView imageView) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            ((Activity) context).startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickPictureIntent(ImageView imageView) {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        ((Activity) context).startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data, ImageView imageView) {
        if (resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri selectedImage = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap byteArrayToBitmap(byte[] imageBytes) {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void showDeleteConfirmationDialog(Etudiant etudiant, int position) {
        new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme)
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer " + etudiant.getNom() + "?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    etudiantService.delete(etudiant);
                    etudiantList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    public static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvNom, tvPrenom, tvDateNaissance;
        ImageView ivPhoto;

        public EtudiantViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvNom = itemView.findViewById(R.id.tvNom);
            tvPrenom = itemView.findViewById(R.id.tvPrenom);
            tvDateNaissance = itemView.findViewById(R.id.tvDateNaissance);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }
}
