package com.example.aipersonas.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aipersonas.R;

public class SplashActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        };

        handler.postDelayed(runnable, 2500);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // app was lagging, maybe because of the splash screen.
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}