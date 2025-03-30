package com.emsi.tp4_sqlite;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.tp4_sqlite.adapter.EtudiantAdapter;
import com.emsi.tp4_sqlite.classes.Etudiant;
import com.emsi.tp4_sqlite.service.EtudiantService;

import java.util.List;

public class ListActivityEtudiant extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;
    private EtudiantService etudiantService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiants);

        // Configuration de la Toolbar (vérifiez qu'elle existe dans votre layout)
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Liste des Étudiants");
            }
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etudiantService = new EtudiantService(this);
        List<Etudiant> etudiants = etudiantService.findAll();

        adapter = new EtudiantAdapter(this, etudiants);

        // Animation simplifiée sans SlideInLeftAnimationAdapter
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}