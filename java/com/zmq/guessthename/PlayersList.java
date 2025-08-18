package com.zmq.guessthename;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
public class PlayersList {
//    Images are assign according to index number of following ArrayList
    ArrayList<String> playerList = new ArrayList<>(Arrays.asList(
            "Rohit Sharma",
            "Shubman Gill",
            "Virat Kohli",
            "Shreyas Iyer",
            "KL Rahul",
            "Rishabh Pant",
            "Hardik Pandya",
            "Axar Patel",
            "Washington Sundar",
            "Kuldeep Yadav",
            "Harshit Rana",
            "Mohammed Shami",
            "Arshdeep Singh",
            "Varun Chakaravarthy",
            "Ravindra Jadeja",
            "Mohammed Siraj"
    ));

//    size of list
    public int size(){
        return playerList.size();
    }

//    choose player
    public int chosenPlayerIndex(){
        Random random = new Random();
        return random.nextInt(size());
    }

    public Bitmap getPlayerImage(@NonNull Context context, int index){
        int resId = context.getResources().getIdentifier(
                "p" + index,
                "drawable",
                context.getPackageName()
        );
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }
}
