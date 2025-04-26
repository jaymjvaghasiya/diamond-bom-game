package com.example.dimondgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StartGameActivity extends AppCompatActivity {

    TextView name;
    TextView winAmt;
    TextView betAmt;
    Button withdraw;

    HashSet<Integer> bombArray;
    ImageButton[] img = new ImageButton[16];

    Double betAmount;
    Double winAmount;

    String apiUrl = "https://diamondgame2.onrender.com/api/users/credit/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.tvStartGameName);
        winAmt = findViewById(R.id.tvStartGameWinAmt);
        betAmt = findViewById(R.id.tvStartGameBetAmt);
        withdraw = findViewById(R.id.btnStartGameWithdraw);

        SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String fnm = sp.getString("firstname", "User").replace("\"", "");
        betAmount = Double.parseDouble(getIntent().getStringExtra("bet_amount").toString());
        winAmount = 0.0;
        String uid = sp.getString("userId", "-1");
        String token = sp.getString("token", "-1");

        name.setText(fnm);
        winAmt.setText(winAmount.toString());
        betAmt.setText(betAmount.toString());

        img[0] = findViewById(R.id.imgBtn0);
        img[1] = findViewById(R.id.imgBtn1);
        img[2] = findViewById(R.id.imgBtn2);
        img[3] = findViewById(R.id.imgBtn3);
        img[4] = findViewById(R.id.imgBtn4);
        img[5] = findViewById(R.id.imgBtn5);
        img[6] = findViewById(R.id.imgBtn6);
        img[7] = findViewById(R.id.imgBtn7);
        img[8] = findViewById(R.id.imgBtn8);
        img[9] = findViewById(R.id.imgBtn9);
        img[10] = findViewById(R.id.imgBtn10);
        img[11] = findViewById(R.id.imgBtn11);
        img[12] = findViewById(R.id.imgBtn12);
        img[13] = findViewById(R.id.imgBtn13);
        img[14] = findViewById(R.id.imgBtn14);
        img[15] = findViewById(R.id.imgBtn15);

        bombArray = new HashSet<>();
        while(bombArray.size() != 4) {
            Integer r = (int)(Math.random() * 15);
            bombArray.add(r);
        }

        for(Integer i = 0; i < img.length; i++) {
            Integer index = i;
            img[i].setOnClickListener(v -> gamePlay(index));
        }

        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String withdrawAmt = winAmt.getText().toString();
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<Integer> ft = executorService.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return creditAmount(uid, withdrawAmt, token);
                    }
                });

                try {
                    Integer statusCode = ft.get();
                    Log.i("SHARED_PREF_CREDIT", statusCode+"");
                    if(statusCode == 200) {
                        Toast.makeText(getApplicationContext(), "Amount Updated Successfully.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                        setResult(RESULT_OK, intent);
                        startActivity(intent);
                    }
                } catch(ExecutionException e) {
                    throw new RuntimeException(e);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void gamePlay(Integer index) {
        Log.i("GamePlay", img[index]+"");
        Log.i("GamePlay", "Seting images" + img[index].getBackground().toString());

        if(bombArray.contains((index))) {
            img[index].setBackground(getDrawable(R.drawable.bomb));
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.bomb_explosion);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);

            SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
            String uid = sp.getString("userId", "-1");
            String token = sp.getString("token", "-1");
            String betAmount = String.valueOf((int)Double.parseDouble(betAmt.getText().toString())*-1);

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Integer> ft = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return creditAmount(uid, betAmount, token);
                }
            });

            try {
                Integer statusCode = ft.get();
                if(statusCode == 200) {
                    Toast.makeText(getApplicationContext(), "Amount Updated Successfully.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                    setResult(RESULT_OK, intent);
                }
            } catch(ExecutionException e) {
                throw new RuntimeException(e);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }

            Toast.makeText(getApplicationContext(), "Oops! Plz Try Again!!!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(intent);

        } else {
            if(img[index].getBackground().toString().contains("RippleDrawable")) {
                img[index].setBackground(getDrawable(R.drawable.dimond));

                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.diamond);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);

                winAmount += betAmount;
                winAmt.setText(winAmount.toString());
            }
        }
    }

    private Integer creditAmount(String uid, String winAmount, String token) {

        String cleanedToken = token.replace("\"", "");
        Log.i("apiurl", "Bearer " + cleanedToken);

        apiUrl = apiUrl + uid.replace("\"", "");
        Log.i("apiurl", apiUrl);

        Map<String, Integer> data = new HashMap<>();
        data.put("credit", (int)Double.parseDouble(winAmount));

        Gson gson = new Gson();
        String dataStr = gson.toJson(data);

        Log.i("apiurl", dataStr);

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", cleanedToken);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            out.write(dataStr.getBytes());
            out.flush();
            out.close();

            Integer responseCode = connection.getResponseCode();
            Log.i("HTTP_STATUS", String.valueOf(responseCode));

            if (responseCode >= 400) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String line;
                StringBuilder errorBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    errorBuilder.append(line);
                }
                Log.e("API_ERROR", errorBuilder.toString());
            }

            SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            Integer currentCredit = sp.getInt("credit", 0);
            Integer betAmount = (int)Double.parseDouble(winAmount);
            Integer updatedCredit = currentCredit + betAmount;
            editor.putInt("credit", updatedCredit);
            editor.apply();


            Log.i("SHARED_PREF_CREDIT", "Updated credit saved: " + updatedCredit);

            return responseCode;

        } catch(Exception e) {
            Log.i("NETWORK_ERROR", "Exception occurred: " + e.getMessage(), e);
        }

        return -1;
    }
}