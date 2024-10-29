package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.models.Persona;
import com.example.aipersonas.repositories.PersonaRepository;

import java.util.List;

public class PersonaViewModel extends AndroidViewModel {

    private PersonaRepository personaRepository;
    private LiveData<List<Persona>> allPersonas;

    public PersonaViewModel(@NonNull Application application) {
        super(application);
        personaRepository = new PersonaRepository(application);
        allPersonas = personaRepository.getAllPersonas();
    }

    public LiveData<List<Persona>> getAllPersonas() {
        return allPersonas;
    }

    public void insert(Persona persona) {
        personaRepository.insert(persona);
    }

    // Similarly, you can add update and delete methods if required.
}
