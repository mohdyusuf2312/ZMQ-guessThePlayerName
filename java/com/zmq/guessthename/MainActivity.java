package com.zmq.guessthename;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    private void showGameOverDialog(Bitmap full, String chosenPlayer, String color) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[] {
                        Color.parseColor("#413CA6"),
                        Color.parseColor("#5353B8"),
                        Color.parseColor("#4BC1C9"),
                        Color.parseColor("#00D0FF"),
                        Color.parseColor("#185E94")
                }
        );
        bg.setCornerRadius(16f);
        View gradientBG = dialog.findViewById(R.id.dialog);
        gradientBG.setBackground(bg);

        if (color.equals("green")) {
            TextView resultText = dialog.findViewById(R.id.result);
            resultText.setText("You won.");
            resultText.setTextColor(Color.parseColor("#4CAF50"));
        } else if (color.equals("red")) {
            TextView resultText = dialog.findViewById(R.id.result);
            resultText.setText("You Lose.");
            resultText.setTextColor(Color.parseColor("#FA2D1E"));
        }

        ImageView overImage = dialog.findViewById(R.id.overImage);
        overImage.setImageBitmap(full);

        TextView overName = dialog.findViewById(R.id.overName);
        overName.setText("The player was " + chosenPlayer);

        Button btnPlay = dialog.findViewById(R.id.btnPlay);
        Button btnQuit = dialog.findViewById(R.id.btnQuit);

        btnPlay.setOnClickListener(fn -> {
            lives = 4;
            playGame();
            dialog.dismiss();
        });

        btnQuit.setOnClickListener(fn -> {
            finish();
        });

        dialog.show();

    }

    @SuppressLint("SetTextI18n")
    public void playGame(){

//        Display lives
        String livesString = "Lives: " + "❤️".repeat(lives);
        TextView live= findViewById(R.id.lives);
        live.setText(livesString);

//        choose player
        PlayersList playersList = new PlayersList();
        int chosenPlayerIndex = playersList.chosenPlayerIndex();
        String chosenPlayer = (playersList.playerList.get(chosenPlayerIndex)).toUpperCase();

//        Hidden Characters
        StringBuilder hiddenWord = new StringBuilder();
        for(int i = 0; i < chosenPlayer.length(); i++){
            if(chosenPlayer.charAt(i) != ' '){
                hiddenWord.append("_");
            } else {
                hiddenWord.append(" ");
            }
        }

        TextView playerTextView = findViewById(R.id.playerTextView);
        playerTextView.setText(hiddenWord);


//        Image chosen
        Bitmap full = playersList.getPlayerImage(this, chosenPlayerIndex);

        int partWidth = full.getWidth()/3;
        int partHeight = full.getHeight()/3;

//        Assign cropped image to particular grid
        int index = 0;
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 3; column++){
                Bitmap cropped = Bitmap.createBitmap(full, column * partWidth, row * partHeight, partWidth, partHeight);
                ImageViews[index].setImageBitmap(cropped);
                ImageViews[index].setVisibility(ImageView.INVISIBLE);
                index++;
            }
        }

//        Track opened grid indexes
        ArraySet <Integer> openGrid = new ArraySet<>();

//        Visible two random unique grid initially
        Random random = new Random();
        while (openGrid.size() < 2){
            int randomIndex = random.nextInt(9);
            if (!openGrid.contains(randomIndex)){
                ImageViews[randomIndex].setVisibility(ImageView.VISIBLE);
                openGrid.add(randomIndex);
            }
        }

        ArrayList<Character> guessLetters = new ArrayList<>();

        Button btn = findViewById(R.id.btnSubmit);
        btn.setOnClickListener(fn -> {

//           guess Character
            EditText editText = findViewById(R.id.guessChar);
            String guessInput = (editText.getText().toString()).toUpperCase();

            if(guessInput.isEmpty()){
                Toast.makeText(this, "Please enter a Character.", Toast.LENGTH_SHORT).show();
                return;
            } else if (guessInput.matches("[0-9]")) {
                Toast.makeText(this, "Numbers are not acceptable.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                boolean foundInGuessLetters = false;
                char guessChar = guessInput.charAt(0);
                for (char c: guessLetters){
                    if (c == guessChar){
                        foundInGuessLetters = true;
                        Toast.makeText(this, "Already guessed", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                if (!foundInGuessLetters) {
                    guessLetters.add(guessChar);

                    boolean foundInChosenName = false;
                    for(int i = 0; i < chosenPlayer.length(); i++){
                        if(chosenPlayer.charAt(i) == guessChar){
                            hiddenWord.setCharAt(i, guessChar);
                            foundInChosenName = true;
                        }
                    }

                    if(!foundInChosenName){
                        if (openGrid.size() < 9) {
                            int randomIndex;
                            do {
                                randomIndex = random.nextInt(9);
                            } while (openGrid.contains(randomIndex));
//                        Add new open grid
                            openGrid.add(randomIndex);
                            ImageViews[randomIndex].setVisibility(ImageView.VISIBLE);
                        }
                        lives--;
                        if (lives > 0) {
                            Toast.makeText(this, "You guessed '" + guessChar + "'. That is not in the word. You lose a life.", Toast.LENGTH_SHORT).show();
                            live.setText("Lives: " + "❤️".repeat(lives));
                        } else {
                            live.setText("Lives: " + "❤️".repeat(0));
                            showGameOverDialog(full, chosenPlayer, "red");
                            editText.setText("");
                            return;
                        }
                    }
                }

                boolean foundBlank = false;
                for (int j = 0; j < hiddenWord.length(); j++) {
                    if (hiddenWord.charAt(j) == '_'){
                        foundBlank = true;
                        break;
                    }
                }
                if (!foundBlank) {
                    showGameOverDialog(full, chosenPlayer, "green");
                }
//                Update UI
                playerTextView.setText(hiddenWord);
            }
            editText.setText("");
        });
    }

//   Max wrong guess
    int lives = 4;

//    Image grid
    private final ImageView[] ImageViews = new ImageView[9];

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText editText = findViewById(R.id.guessChar);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnSubmit.performClick(); // submit as if button was clicked
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[] {
                        Color.parseColor("#413CA6"),
                        Color.parseColor("#5353B8"),
                        Color.parseColor("#4BC1C9"),
                        Color.parseColor("#00D0FF"),
                        Color.parseColor("#185E94")
                }
        );
        bg.setCornerRadius(16f);
        findViewById(R.id.main).setBackground(bg);

//        Image Grid
        ImageViews[0] = findViewById(R.id.image_0);
        ImageViews[1] = findViewById(R.id.image_1);
        ImageViews[2] = findViewById(R.id.image_2);
        ImageViews[3] = findViewById(R.id.image_3);
        ImageViews[4] = findViewById(R.id.image_4);
        ImageViews[5] = findViewById(R.id.image_5);
        ImageViews[6] = findViewById(R.id.image_6);
        ImageViews[7] = findViewById(R.id.image_7);
        ImageViews[8] = findViewById(R.id.image_8);

        playGame();
    }
}