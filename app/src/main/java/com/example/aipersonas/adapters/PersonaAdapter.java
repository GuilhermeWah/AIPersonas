package com.example.aipersonas.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aipersonas.R;
import com.example.aipersonas.models.Persona;

import java.util.List;

public class PersonaAdapter extends RecyclerView.Adapter<PersonaAdapter.PersonaViewHolder> {

    private List<Persona> personaList;
    private final OnPersonaClickListener chatClickListener;
    private final OnPersonaLongClickListener longClickListener;

    public PersonaAdapter(List<Persona> personaList, OnPersonaClickListener clickListener, OnPersonaLongClickListener longClickListener) {
        this.personaList = personaList;
        this.chatClickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public interface OnPersonaClickListener {
        void onPersonaClick(Persona persona);
    }

    public interface OnPersonaLongClickListener {
        void onPersonaLongClick(Persona persona);
    }

    public void setPersonaList(List<Persona> personaList) {
        this.personaList = personaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PersonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_persona_card, parent, false);
        return new PersonaViewHolder(view);
    }



    //nov 5th --> we are changing it since the flow of our project activity has changed.
    //Now, instead of opening the chat activity, we have to open the ChatList Activity.
    @Override
    public void onBindViewHolder(@NonNull PersonaViewHolder holder, int position) {
        Persona persona = personaList.get(position);
        holder.title.setText(persona.getName());
        Log.d("Adapter", "Binding persona at position: " + position + ", name: " + persona.getName() + ", description: " + persona.getPersonaDescription());

        holder.description.setText(persona.getPersonaDescription()); // Display only the user-provided description

        holder.itemView.setOnClickListener(v -> chatClickListener.onPersonaClick(persona));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onPersonaLongClick(persona);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return personaList != null ? personaList.size() : 0;
    }

    static class PersonaViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;

        public PersonaViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.personaTitle);
            description = itemView.findViewById(R.id.personaDescription);
        }
    }
}
