package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import com.example.aipersonas.R;

import com.example.aipersonas.adapters.PersonaAdapter;
import com.example.aipersonas.models.Persona;
import com.example.aipersonas.repositories.PersonaRepository;
import com.example.aipersonas.repositories.UserRepository;
import com.example.aipersonas.viewmodels.PersonaViewModel;
import com.example.aipersonas.viewmodels.UserViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PersonaAdapter.OnPersonaClickListener, PersonaAdapter.OnPersonaLongClickListener {

    private RecyclerView personaRecyclerView;
    private PersonaAdapter personaAdapter;
    private Button createNewChatButton;
    private Button searchChatButton;
    private PersonaRepository personaRepository;
    private BottomNavigationView bottomNavigationView;
    private PersonaViewModel personaViewModel;
    private UserViewModel userViewModel;
    private UserRepository userRepository;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        personaRecyclerView = findViewById(R.id.personaRecyclerView);
        createNewChatButton = findViewById(R.id.createNewChatButton);
        searchChatButton = findViewById(R.id.searchChatButton);
        personaRepository = new PersonaRepository(getApplication());

        // Set up RecyclerView with Grid Layout
        personaRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));


        // here we are not using a populated list;  since we are using LiveData (from ViewModel)
        //  it will be updated via LiveData
        personaAdapter = new PersonaAdapter(new ArrayList<>(), this, this);
        personaRecyclerView.setAdapter(personaAdapter);


/* //DUMMY DATA TEST  FOR THE RECYCLER VIEW
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
        personaRecyclerView.setAdapter(personaAdapter);*/

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUser().observe(this, user -> {
                   if(user != null) {
                       TextView userNameTextView = findViewById(R.id.userName);
                       userNameTextView.setText(user.getFullName());
                   }
                });


        // VIEWMODEL TO UPDATE IN REAL TIME CHANGES ON THE DATA AND UI
        // Initialize ViewModel
        personaViewModel = new ViewModelProvider(this).get(PersonaViewModel.class);

        // Observe persona list from ViewModel
        personaViewModel.getAllPersonas().observe(this, personas -> {
            if (personas != null) {
                personaAdapter.setPersonaList(personas);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Settings":
                    startActivity(new Intent(MainActivity.this, UserSettings.class));
                    //Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show();
                    return true;

                case "Home":
                    startActivity(new Intent(MainActivity.this, ChatListActivity.class));
                   // Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
                    return true;

                case "Create Chat":
                    showCreateChatDialog();

                    return true;

                case "Search":
                   // Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show();
                    return true;

                default:
                    return false;
            }
        });

        // create new persona chat button
        createNewChatButton.setOnClickListener(v -> showCreateChatDialog());

        // @TODO: search chat button
        searchChatButton.setOnClickListener(v -> {
            // Implement search functionality
            Toast.makeText(MainActivity.this, "Search Chat functionality coming soon...", Toast.LENGTH_SHORT).show();
        });
    }

    //  show dialog for creating a new persona/chat
    private void showCreateChatDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.create_personamodal, null);

        // Initialize the EditTexts from the modal  layout we created
        EditText personaTitleEditText = dialogView.findViewById(R.id.editTextPersonaTitle);
        EditText personaDescriptionEditText = dialogView.findViewById(R.id.editTextPersonaDescription);

        // build and show the modal
        new AlertDialog.Builder(this)
                .setTitle("Create New Persona")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title = personaTitleEditText.getText().toString().trim();
                    String description = personaDescriptionEditText.getText().toString().trim();

                    if (!title.isEmpty() && !description.isEmpty()) {
                        // here we are creating the persona and storing it on the db
                        Persona newPersona = new Persona(title, description);
                        personaViewModel.insert(newPersona);

                        Toast.makeText(MainActivity.this, "Persona created successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a title and description!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deletePersona(Persona persona) {
        personaViewModel.delete(persona, new PersonaRepository.FirestoreCallback() {
            @Override
            public void onSuccess() {
                // Persona successfully deleted
                Toast.makeText(MainActivity.this, "Persona deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                // Failed to delete persona
                Toast.makeText(MainActivity.this, "Failed to delete persona: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*   @Override
      public void onPersonaClick(Persona persona) {
          // Open ChatActivity with selected persona
          Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
          intent.putExtra("personaId", persona.getPersonaId());
          startActivity(intent);

      }*/
    @Override
    public void onPersonaClick(Persona persona) {
        if (persona != null) {
            Log.d("MainActivity", "Persona clicked: " + persona.getPersonaId());
            Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
            intent.putExtra("personaId", persona.getPersonaId());
            startActivity(intent);
        } else {
            Log.e("MainActivity", "Persona is null, unable to start ChatListActivity.");
        }
    }

    @Override
    public void onPersonaLongClick(Persona persona) {
        // Create an AlertDialog to give user options for delete or favorite
        new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setItems(new String[]{"Delete", "Favorite"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Delete
                            deletePersona(persona);
                            break;
                        case 1: // Favorite
                            //@TODO:favoritePersona(persona);
                            break;
                    }
                })
                .show();
    }
    //dummy datam just to test.
    @NonNull
    private List<Persona> createDummyPersonas() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona( "Cybersecurity Expert", "Protects your data and enhances digital security."));
        personas.add(new Persona( "Culture Specialist", "Helps with translations and cultural insights."));
        personas.add(new Persona( "Medical Advisor", "Offers general health and wellness tips."));
        personas.add(new Persona( "Financial Consultant", "Guides you in budgeting and investments."));
        personas.add(new Persona( "Mental Health Coach", "Supports emotional well-being and mindfulness."));
        personas.add(new Persona( "Travel Planner", "Plans your trips with custom itineraries."));
        personas.add(new Persona( "Fitness Instructor", "Provides workout routines and fitness advice."));
        personas.add(new Persona( "Recipe Guru", "Suggests easy recipes for every occasion."));
        personas.add(new Persona( "History Enthusiast", "Explores major historical events and figures."));
        return personas;
    }


}

