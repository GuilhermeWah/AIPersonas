package com.example.aipersonas.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.aipersonas.models.User;
import com.example.aipersonas.repositories.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<User> getUser() {
        return userRepository.getUserById();
    }

    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    public void deleteUser() {
        userRepository.deleteUser();
    }
}
