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

    /**
     * Deletes the user from Firestore and local Room database.
     * The deletion is carried out synchronously to ensure no inconsistency between cloud and local storage.
     * Thats the reason we created the interface, just so we notify the caller.
     * ====================@Todo: Lots of threads running, we have to figure it out.
     * @param callback Just a callback to notify the outcome of the deletion operation (success or failure).
     */
    public void deleteUser(UserRepository.DeleteUserCallback callback) {
        userRepository.deleteUser(callback);
    }
}
