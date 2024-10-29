package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.adapters.PersonaAdapter;
import com.example.aipersonas.models.Persona;
import com.example.aipersonas.viewmodels.PersonaViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PersonaAdapter.OnPersonaClickListener, PersonaAdapter.OnPersonaLongClickListener {

    private RecyclerView personaRecyclerView;
    private PersonaAdapter personaAdapter;
    private Button createNewChatButton;
    private Button searchChatButton;

    private PersonaViewModel personaViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personaRecyclerView = findViewById(R.id.personaRecyclerView);
        createNewChatButton = findViewById(R.id.createNewChatButton);
        searchChatButton = findViewById(R.id.searchChatButton);

        //just for test purposes
        List<Persona> dummyPersonas = createDummyPersonas();


        // Set up RecyclerView with Grid Layout
        personaRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        //personaAdapter = new PersonaAdapter(dummyPersonas,this, this);


 //DUMMY DATA TEST  FOR THE RECYCLER VIEW
        personaAdapter = new PersonaAdapter(createDummyPersonas(),
                persona -> {
                    // Handle persona click event here, e.g., open ChatActivity
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("personaName", persona.getDescription());
                    startActivity(intent);
                },
                persona -> {
                    // Handle persona long click event here, e.g., show delete or favorite options
                    Toast.makeText(MainActivity.this, persona.getDescription() + " long clicked!", Toast.LENGTH_SHORT).show();
                }
        );

        personaRecyclerView.setAdapter(personaAdapter);
   /*
        // VIEWMODEL TO UPDATE IN REAL TIME CHANGES ON THE DATA AND UI
        // Initialize ViewModel
        personaViewModel = new ViewModelProvider(this).get(PersonaViewModel.class);

        // Observe persona list from ViewModel
        personaViewModel.getAllPersonas().observe(this, personas -> {
            if (personas != null) {
                personaAdapter.setPersonaList(personas);

            }
        });

        */

        // Create New Chat Button Action
        createNewChatButton.setOnClickListener(v -> {
            // Redirect to Create Persona Activity
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Search Chat Button Action
        searchChatButton.setOnClickListener(v -> {
            // Implement search functionality
            Toast.makeText(MainActivity.this, "Search Chat functionality coming soon...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPersonaClick(Persona persona) {
        // Open ChatActivity with selected persona
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra("personaId", persona.getPersonaId());
        startActivity(intent);
    }

    @Override
    public void onPersonaLongClick(Persona persona) {
        // Display options for favorite or delete persona
        // Implement context menu or dialog for additional actions
        Toast.makeText(this, "Long press options: Favorite or Delete", Toast.LENGTH_SHORT).show();
    }

    private List<Persona> createDummyPersonas() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona("Cybersecurity Expert", "Protects your data and enhances digital security."));
        personas.add(new Persona("Culture Specialist", "Helps with translations and cultural insights."));
        personas.add(new Persona("Medical Advisor", "Offers general health and wellness tips."));
        personas.add(new Persona("Financial Consultant", "Guides you in budgeting and investments."));
        personas.add(new Persona("Mental Health Coach", "Supports emotional well-being and mindfulness."));
        personas.add(new Persona("Travel Planner", "Plans your trips with custom itineraries."));
        personas.add(new Persona("Fitness Instructor", "Provides workout routines and fitness advice."));
        personas.add(new Persona("Recipe Guru", "Suggests easy recipes for every occasion."));
        personas.add(new Persona("History Enthusiast", "Explores major historical events and figures."));
        return personas;
    }
}

