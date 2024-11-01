package com.example.aipersonas.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

    //we gonna use this class just to check if the device is connected to the internet
public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}