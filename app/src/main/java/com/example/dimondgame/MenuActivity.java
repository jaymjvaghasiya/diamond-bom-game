package com.example.dimondgame;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MenuActivity extends AppCompatActivity {

    TextView name;
    TextView credit;
    Button playGame;
    Button leaderBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.tvMenuActName);
        credit = findViewById(R.id.tvMenuActCredit);
        playGame = findViewById(R.id.btnMenuActPlay);
        leaderBoard = findViewById(R.id.btnMenuActLeaderBoard);

        SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
        String fnm = sp.getString("firstname", "User").replace("\"", "");
        Integer creditAmount = sp.getInt("credit", 0);
        name.setText(fnm);
        credit.setText("Credit: " + String.valueOf(creditAmount));

        playGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.i("playgame", "in game");
                LayoutInflater inflater = LayoutInflater.from(MenuActivity.this);
                View dialogView = inflater.inflate(R.layout.activity_dilog_bet_amount, null);

                AlertDialog dialog = new AlertDialog.Builder(MenuActivity.this)
                        .setView(dialogView)
                        .create();

                EditText etBetAmount = dialogView.findViewById(R.id.etBetAmount);
                Button btnBat = dialogView.findViewById(R.id.btnBat);

                btnBat.setOnClickListener(batView -> {
                    String amount = etBetAmount.getText().toString().trim();
                    Integer betAmount = Integer.parseInt(etBetAmount.getText().toString().trim());
                    if(!amount.isBlank() && betAmount < creditAmount) {
                        Intent intent = new Intent(getApplicationContext(), StartGameActivity.class);
                        intent.putExtra("bet_amount", amount);
                        startActivityForResult(intent, 100);
                        dialog.dismiss();
                    } else if(betAmount > creditAmount) {
                        Toast.makeText(getApplicationContext(), "Insufficient credits.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please Enter bet amount", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.show();
            }
        });

        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LeaderBoardActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
            int creditAmount = sp.getInt("credit", 0);
            credit.setText("Credit: " + creditAmount);
            Log.i("ON_RESULT_CREDIT", "Updated after game: " + creditAmount);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (credit != null) {
            SharedPreferences sp = getSharedPreferences("dimondBomb", MODE_PRIVATE);
            Integer creditAmount = sp.getInt("credit", 0);
            Log.i("ON_RESUME_CREDIT", "Read credit: " + creditAmount);
            credit.setText("Credit: " + creditAmount);
        }
    }
}