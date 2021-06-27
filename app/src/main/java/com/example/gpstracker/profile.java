package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class profile extends AppCompatActivity {

    SessionManager sessionManager;
    TextView ids;
    Button logoutbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ids = (TextView) findViewById(R.id.txtid);
        logoutbtn = (Button) findViewById(R.id.btnlogout);

        sessionManager = new SessionManager(profile.this);

        ids.setText("ID GPS : "+sessionManager.getUserDetail().get(SessionManager.ID_GPS));

        //initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set home selected
        bottomNavigationView.setSelectedItemId(R.id.profile);

        //perform itemselected listener

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.profile:
                        return true;

                    case R.id.home:
                        startActivity(new Intent(profile.this,trackinggps.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.about:
                        startActivity(new Intent(profile.this,about.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        sessionManager = new SessionManager(profile.this);
        if (!sessionManager.isLoggedIn()) {
            movetoLogin();
        }

        logoutbtn.setOnClickListener(v -> {
            sessionManager.logoutSession();
            movetoLogin();
        });
    }

    private void movetoLogin() {
        Intent i = new Intent(profile.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        finish();
    }
}