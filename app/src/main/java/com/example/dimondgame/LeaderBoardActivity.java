package com.example.dimondgame;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.UserModel;

public class LeaderBoardActivity extends AppCompatActivity {

    ListView allPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_board);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        allPlayers = findViewById(R.id.listLeaderBoardActPlayears);


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<UserModel>> ft = executorService.submit(new Callable<List<UserModel>>() {
            @Override
            public List<UserModel> call() throws Exception {
                return getAllplayears();
            }
        });

        try {
            List<UserModel> leader = ft.get();
            ArrayAdapter<UserModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leader);
            allPlayers.setAdapter(adapter);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<UserModel> getAllplayears() {
        List<UserModel> list = new ArrayList<>();
        String apiUrl = "https://diamondgame2.onrender.com/api/users/leaderboard";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setRequestProperty("Accept","application/json");

            InputStream ins = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            StringBuffer data = new StringBuffer();
            String playears = "";
            while((playears = br.readLine()) != null) {
                data.append(playears);
            }

            Gson gson = new Gson();
            Type userListType = new TypeToken<List<UserModel>>() {}.getType();
            list = gson.fromJson(data.toString(), userListType);

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}