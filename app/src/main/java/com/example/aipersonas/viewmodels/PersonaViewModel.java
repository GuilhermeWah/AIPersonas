package com.example.aipersonas.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aipersonas.models.Persona;
import com.example.aipersonas.repositories.GPTRepository;
import com.example.aipersonas.repositories.PersonaRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PersonaViewModel extends AndroidViewModel {

    private static final String TAG = "PersonaViewModel";
    private final PersonaRepository personaRepository;
    private final GPTRepository gptRepository;
    private final LiveData<List<Persona>> allPersonas;
    private final MutableLiveData<String> tailoredDescriptionLiveData = new MutableLiveData<>();

    public PersonaViewModel(@NonNull Application application) {
        super(application);
        personaRepository = new PersonaRepository(application);
        gptRepository = new GPTRepository(application);
        allPersonas = personaRepository.getAllPersonas();
    }

    public void tailorPersonaDescription(String personaId, String description) {
        Log.d(TAG, "Tailoring description with GPT-3.5");


        // Call the method from GPTRepository to handle the request
        gptRepository.sendGPTRequest(description,400, 0.6F,new GPTRepository.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Extract the tailored description from the response JSON
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String tailoredDescription = message.getString("content");

                        Log.d(TAG, "GPT tailored description received: " + tailoredDescription);

                        // Store tailored description in Firestore and Room
                        personaRepository.updatePersonaDescription(personaId, tailoredDescription, description);
                        tailoredDescriptionLiveData.postValue(tailoredDescription);
                    } else {
                        Log.e(TAG, "No choices found in GPT response.");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse GPT response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to tailor persona description: " + error);
                // Handle error (e.g., show a toast)
            }
        });
    }





        public LiveData<List<Persona>> getAllPersonas() {
        return allPersonas;
    }

    public void insert(Persona persona) {
        personaRepository.insert(persona);
    }

    public LiveData<String> getTailoredDescriptionLiveData() {
        return tailoredDescriptionLiveData;
    }

    public void delete(Persona persona, PersonaRepository.FirestoreCallback callback) {
        personaRepository.delete(persona, callback);
    }

    public LiveData<Persona> getPersonaById(String personaId) {
        return personaRepository.getPersonaById(personaId);
    }

    public LiveData<String> getTailoredDescription() {
        return tailoredDescriptionLiveData;
    }

    public void updatePersonaDescription(String personaId, String tailoredDescription) {
        personaRepository.updatePersonaDescription(personaId, tailoredDescription, "" );
    }




}
