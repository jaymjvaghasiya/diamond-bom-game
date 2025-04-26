package com.example.dimondgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    TextView signup;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.edtLoginActUsername);
        password = findViewById(R.id.edtLoginActPassword);
        signup = findViewById(R.id.tvLoginActSignupLink);
        submit = findViewById(R.id.btnLoginActSubmit);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String unm = username.getText().toString();
                String pass = password.getText().toString();
                Boolean isError = false;

                if(unm.isBlank()) {
                    username.setError("Please Enter Username");
                    isError = true;
                }
                if(pass.isBlank()) {
                    password.setError("Please Enter Password");
                    isError = true;
                }

                if(isError) {
                    Toast.makeText(getApplicationContext(), "Please Fill the required Fields.", Toast.LENGTH_LONG).show();
                } else {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    Future<Integer> fStatus = executorService.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            return Authenticate(unm, pass);
                        }
                    });

                    try {
                        Integer statusCode = fStatus.get();

                        runOnUiThread(() -> {
                            if(statusCode == 200) {
                                Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                                finish(); // Optional: remove signup screen from back stack
                            } else {
                                Toast.makeText(getApplicationContext(), "Login Failed: " + statusCode, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch(Exception e) {
                        Log.i("NETWORK_ERROR", "Exception occured : " + e.getMessage(), e);
                    }
                }
            }
        });
    }

    public Integer Authenticate(String unm, String pass) {

        Map<String, Object> data = new HashMap<>();
        data.put("email", unm);
        data.put("password", pass);

        Gson gson = new Gson();
        String dataStr = gson.toJson(data);

        Log.i("data", dataStr);

        try {
            URL url = new URL("" +
                    "https://diamondgame2.onrender.com/api/auth/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            out.write(dataStr.getBytes());
            out.flush();
            out.close();

            int responseCode = connection.getResponseCode();
            Log.i("HTTP_STATUS", String.valueOf(responseCode));

            InputStream is;
            if (responseCode >= 400) {
                is = connection.getErrorStream();
            } else {
                is = connection.getInputStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
            String token = jsonObject.get("token").toString();

            JsonObject jsonObjectUser = gson.fromJson(jsonObject.get("user").toString(), JsonObject.class);
            String _id = jsonObjectUser.get("_id").toString();
            String firstname = jsonObjectUser.get("firstName").toString();
            String lastname = jsonObjectUser.get("lastName").toString();
            Integer credit = jsonObjectUser.get("credit").getAsInt();
            String email = jsonObjectUser.get("email").toString();
            String userId = jsonObjectUser.get("_id").toString();

            SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("firstname", firstname);
            editor.putString("lastname", lastname);
            editor.putString("email", email);
            editor.putString("token", token);
            editor.putInt("credit", credit);
            editor.putString("userId", userId);
            editor.apply();

            Log.i("CREDIT", credit + "");

            Log.i("HTTP_RESPONSE", response.toString());

            return responseCode;

        } catch(Exception e) {
            Log.i("NETWORK_ERROR", "Exception occurred: " + e.getMessage(), e);
        }

        return -1;
    }

}