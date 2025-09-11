package com.zmq.guessthename;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.view.View;
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
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public boolean isPlayerWon = false;
    private final ArraySet<Character> guessLetters = new ArraySet<>();
    private TextWatcher textWatcher;
    private EditText editText;

    int score = 0;

    public void updateScore(int lives) {
        if (lives < 4 ) {
            score += (lives * 5);
        }
        else {
            score += 20;
        }
    }

    @SuppressLint("SetTextI18n")
    private void showGameOverDialog(Bitmap full, String chosenPlayer, String color) {
        guessTimer.cancel();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[] {
                        Color.parseColor("#413CA6"),
//                        Color.parseColor("#5353B8"),
                        Color.parseColor("#4BC1C9"),
                        Color.parseColor("#00D0FF"),
                        Color.parseColor("#185E94")
                }
        );
        bg.setCornerRadius(16f);
        View gradientBG = dialog.findViewById(R.id.dialog);
        if (gradientBG != null) {
            gradientBG.setBackground(bg);
        }
        if (color.equals("green")) {
            TextView resultText = dialog.findViewById(R.id.result);
            resultText.setText("You won!");
            resultText.announceForAccessibility("You won");
            resultText.setTextColor(Color.parseColor("#4CAF50"));
        } else if (color.equals("red")) {
            TextView resultText = dialog.findViewById(R.id.result);
            resultText.setText("You Lose!");
            resultText.setTextColor(Color.parseColor("#FA2D1E"));
        }

        TextView Score = dialog.findViewById(R.id.score);
        Score.setText("Your max score: " + score);

        ImageView overImage = dialog.findViewById(R.id.overImage);
        overImage.setImageBitmap(full);

        TextView overName = dialog.findViewById(R.id.overName);
        overName.setText("The player was: " + chosenPlayer);

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

    public CountDownTimer guessTimer;
    public TextView timer;

    public void setGuessTimer(Bitmap full, String chosenPlayer, ArraySet openGrid) {
        if (guessTimer != null){
            guessTimer.cancel();
        }
        guessTimer = new CountDownTimer(20000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText("Time left: " + (millisUntilFinished/1000) + "s");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                Random random = new Random();
                if (openGrid.size() < 9){
                    int randomIndex;
                    do {
                        randomIndex = random.nextInt(9);
                    }while (openGrid.contains(randomIndex));
                    openGrid.add(randomIndex);
                    ImageViews[randomIndex].setImageBitmap(cropped_pieces[randomIndex]);
                }
                lives--;
                if (lives <= 0 && !isPlayerWon){
                    editText.removeTextChangedListener(textWatcher);
                    showGameOverDialog(full, chosenPlayer, "red");
                    score = 0;
                } else {
                    Toast.makeText(MainActivity.this, "Time’s up! You lost 1 life.", Toast.LENGTH_SHORT).show();
                    TextView live = findViewById(R.id.lives);
                    live.setText("Lives: " + "❤️".repeat(Math.max(0, lives)));
                    if (!isPlayerWon){
                        setGuessTimer(full, chosenPlayer, openGrid);
                    }
                }
            }
        }.start();
    }

    @SuppressLint("SetTextI18n")
    public void playGame(){
        guessLetters.clear();
        editText.removeTextChangedListener(textWatcher);
        isPlayerWon = false;

//        score
        TextView textView = findViewById(R.id.score);
        textView.setText("Score: " + score);

//       Display lives
        String livesString = "Lives: " + "❤️".repeat(Math.max(0, lives));
        TextView live= findViewById(R.id.lives);
        live.setText(livesString);

//       choose player
        PlayersList playersList = new PlayersList(this);
        int chosenPlayerIndex = playersList.chosenPlayerIndex();
        String chosenPlayer = (playersList.chosenPlayer(chosenPlayerIndex)).toUpperCase();

//        Hidden Characters
        StringBuilder hiddenWord = new StringBuilder();
        for(int i = 0; i < chosenPlayer.length(); i++){
            if(chosenPlayer.charAt(i) != ' '){
                hiddenWord.append("_");
            } else {
                hiddenWord.append(" ");
            }
        }

        editText.setEms(chosenPlayer.length());

        TextView playerTextView = findViewById(R.id.playerTextView);
        playerTextView.setText(hiddenWord);

//        Display Hint
        TextView hint = findViewById(R.id.hint);
        hint.setText(playersList.getHint(chosenPlayerIndex));

//        Image chosen
        Bitmap full = playersList.getPlayerImage(this, chosenPlayerIndex);
        int partWidth = full.getWidth()/3;
        int partHeight = full.getHeight()/3;

//        Assign cropped image to particular grid
        int index = 0;
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 3; column++){
                Bitmap cropped = Bitmap.createBitmap(full, column * partWidth, row * partHeight, partWidth, partHeight);
                cropped_pieces[index] = cropped;
                ImageViews[index].setImageResource(R.color.transparent);
                index++;
            }
        }
//       Track opened grid indexes
        ArraySet <Integer> openGrid = new ArraySet<>();

//       Visible one random unique grid initially
        Random random = new Random();
        do {
            int randomIndex = random.nextInt(9);
            if (randomIndex != 4 && randomIndex != 1) {
                ImageViews[randomIndex].setImageBitmap(cropped_pieces[randomIndex]);
                openGrid.add(randomIndex);
            }
        } while (openGrid.size() < 1);

        if (!isPlayerWon){
            setGuessTimer(full, chosenPlayer, openGrid);
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0) {
//                   guess Character
                    String guessInput = s.toString().toUpperCase();
                    if(!guessInput.matches("[A-Z]")){
                        Toast.makeText(MainActivity.this, "Please enter a valid letter (A-Z).", Toast.LENGTH_SHORT).show();
                        editText.setText("");
                        return;
                    } else {
                        boolean foundInGuessLetters = false;
                        char guessChar = guessInput.charAt(0);
                        for (char c: guessLetters){
                            if (c == guessChar){
                                foundInGuessLetters = true;
                                Toast.makeText(MainActivity.this, "You already guessed that letter.", Toast.LENGTH_SHORT).show();
                                editText.setText("");
                                return;
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
//                                   Add new open grid
                                    openGrid.add(randomIndex);
                                    ImageViews[randomIndex].setImageBitmap(cropped_pieces[randomIndex]);
                                }
                                lives--;
                                if (lives > 0 && !isPlayerWon) {
                                    Toast.makeText(MainActivity.this, "'" + guessChar + "' is not in the name. You lost 1 life.", Toast.LENGTH_SHORT).show();
                                    live.setText("Lives: " + "❤️".repeat(Math.max(0, lives)));
                                } else {
                                    live.setText("Lives: " + "❤️".repeat(0));
                                    editText.removeTextChangedListener(textWatcher);
                                    showGameOverDialog(full, chosenPlayer, "red");
                                    score = 0;
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
                            isPlayerWon = true;
                            if (guessTimer != null) {
                                guessTimer.cancel();
                            }
                            editText.removeTextChangedListener(textWatcher);
                            updateScore(lives);
                            showGameOverDialog(full, chosenPlayer, "green");
                        }
//                       Update UI
                        playerTextView.setText(hiddenWord);
                    }
                    if (!isPlayerWon){
                        setGuessTimer(full, chosenPlayer, openGrid);
                    }
                    editText.setText("");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        editText.addTextChangedListener(textWatcher);
    }

    //   Max wrong guess
    int lives = 4;
    Bitmap[] cropped_pieces = new Bitmap[9];

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

        timer = findViewById(R.id.timer);
        editText = findViewById(R.id.guessChar);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[] {
                        Color.parseColor("#413CA6"),
//                        Color.parseColor("#5353B8"),
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
