package com.example.dimondgame;

import android.content.Intent;
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

public class SignupActivity extends AppCompatActivity {

    EditText firstname;
    EditText lastname;
    EditText email;
    EditText password;
    Button register;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstname = findViewById(R.id.edtSignupActFirstname);
        lastname = findViewById(R.id.edtSignupActLastname);
        email = findViewById(R.id.edtSignupActEmail);
        password = findViewById(R.id.edtSignupActPassword);
        register = findViewById(R.id.btnSignupActRegister);
        login = findViewById(R.id.tvSignupActLoginLink);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fnm = firstname.getText().toString();
                String lnm = lastname.getText().toString();
                String unm = email.getText().toString();
                String pass = password.getText().toString();
                Boolean isError = false;

                if(fnm.isBlank()) {
                    isError = true;
                    firstname.setError("Please enter your Firstname");
                }
                if(lnm.isBlank()) {
                    isError = true;
                    lastname.setError("Please enter your Lastname");
                }
                if(unm.isBlank()) {
                    isError = true;
                    email.setError("Please enter your email");
                }
                if(pass.isBlank()) {
                    isError = true;
                    password.setError("Please enter password");
                }

                if(isError) {
                    Toast.makeText(getApplicationContext(), "Please Enter Required Fields", Toast.LENGTH_LONG).show();
                } else {
                    ExecutorService executerService = Executors.newSingleThreadExecutor();
                    Future<Integer> fStatus = executerService.submit(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            return registerNewUSer(fnm, lnm, unm, pass);
                        }
                    });

                    try {
                        Integer statusCode = fStatus.get();

                        runOnUiThread(() -> {
                            if (statusCode == 200 || statusCode == 201) {
                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                finish(); // Optional: remove signup screen from back stack
                            } else {
                                Toast.makeText(getApplicationContext(), "Registration Failed: " + statusCode, Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch(Exception e) {
                        Log.i("NETWORK_ERROR", "Exception occurred: " + e.getMessage(), e);
                    }

                }
            }
        });
    }

    public Integer registerNewUSer(String fnm, String lnm, String email, String password) {
        Log.i("REGISTER", "Register button clicked, attempting signup...");
        Map<String, Object> data = new HashMap<>();
        data.put("firstName", fnm);
        data.put("lastName", lnm);
        data.put("email", email);
        data.put("password", password);
        data.put("credit", 5000);

        Gson gson = new Gson();
        String dataStr = gson.toJson(data);

        Log.i("JSON_PAYLOAD", dataStr);

        try {
            URL url = new URL("https://diamondgame2.onrender.com/api/auth/signup");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
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

            Log.i("HTTP_RESPONSE", response.toString());

            return responseCode;

        } catch(Exception e) {
            Log.i("NETWORK_ERROR", "Exception occurred: " + e.getMessage(), e);
        }

        return -1;
    }
}