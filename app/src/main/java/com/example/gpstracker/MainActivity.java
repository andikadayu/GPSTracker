package com.example.gpstracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText etUser, etPass;
    Button btnLogin;
    final String url_login = "https://gps-locations.000webhostapp.com/api/login_android.php";
    final String api_keys = "wco5wkEiVpZumrSru50vZ1imk6knrgMh";
    SessionManager sessionManager;
    LoginData loginData;
    ProgressBar spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUser = (EditText) findViewById(R.id.textUsername);
        etPass = (EditText) findViewById(R.id.textPassword);
        btnLogin = (Button) findViewById(R.id.buttonLogin);

        spinner = (ProgressBar) findViewById(R.id.progBar);
        spinner.setVisibility(View.GONE);

        btnLogin.setOnClickListener(v -> {
            spinner.setVisibility(View.VISIBLE);
            String Username = etUser.getText().toString();
            String Password = etPass.getText().toString();
            new LoginUser().execute(Username, Password);
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class LoginUser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Looper.prepare();
            String Username = strings[0];
            String Password = strings[1];
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("username", Username)
                    .add("password", Password)
                    .add("api_key", api_keys)
                    .build();

            Request request = new Request.Builder()
                    .url(url_login)
                    .post(formBody)
                    .build();
            Response response;
            try {
                response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    if (!result.equalsIgnoreCase("fail")) {

                        sessionManager = new SessionManager(MainActivity.this);
                        loginData = new LoginData();
                        loginData.setId_gps(result);
                        sessionManager.createLoginSession(loginData);

                        Intent i = new Intent(MainActivity.this, trackinggps.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Username or Password mismatch", Toast.LENGTH_LONG).show();
                        spinner.setVisibility(View.GONE);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Looper.loop();
            return null;
        }
    }


}