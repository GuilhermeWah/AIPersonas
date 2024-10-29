package com.example.aipersonas.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.aipersonas.databases.ChatDatabase;
import com.example.aipersonas.databases.PersonaDAO;
import com.example.aipersonas.models.Persona;

import java.util.List;

public class PersonaRepository {

    private PersonaDAO personaDAO;
    private LiveData<List<Persona>> allPersonas;

    // Constructor
    public PersonaRepository(Application application) {
        ChatDatabase database = ChatDatabase.getInstance(application);
        personaDAO = database.personaDao();
        allPersonas = personaDAO.getAllPersonas();
    }

    // Method to return all Personas
    public LiveData<List<Persona>> getAllPersonas() {
        return allPersonas;
    }

    // Method to insert a Persona
    public void insert(Persona persona) {
        new InsertPersonaAsyncTask(personaDAO).execute(persona);
    }

    // Method to update a Persona
    public void update(Persona persona) {
        new UpdatePersonaAsyncTask(personaDAO).execute(persona);
    }

    // Method to delete a Persona
    public void delete(Persona persona) {
        new DeletePersonaAsyncTask(personaDAO).execute(persona);
    }

    // Asynchronous task for inserting a persona
    private static class InsertPersonaAsyncTask extends AsyncTask<Persona, Void, Void> {
        private PersonaDAO personaDAO;

        private InsertPersonaAsyncTask(PersonaDAO personaDAO) {
            this.personaDAO = personaDAO;
        }

        @Override
        protected Void doInBackground(Persona... personas) {
            personaDAO.insert(personas[0]);
            return null;
        }
    }

    // Asynchronous task for updating a persona
    private static class UpdatePersonaAsyncTask extends AsyncTask<Persona, Void, Void> {
        private PersonaDAO personaDAO;

        private UpdatePersonaAsyncTask(PersonaDAO personaDAO) {
            this.personaDAO = personaDAO;
        }

        @Override
        protected Void doInBackground(Persona... personas) {
            personaDAO.update(personas[0]);
            return null;
        }
    }

    // Asynchronous task for deleting a persona
    private static class DeletePersonaAsyncTask extends AsyncTask<Persona, Void, Void> {
        private PersonaDAO personaDAO;

        private DeletePersonaAsyncTask(PersonaDAO personaDAO) {
            this.personaDAO = personaDAO;
        }

        @Override
        protected Void doInBackground(Persona... personas) {
            personaDAO.delete(personas[0]);
            return null;
        }
    }
}
