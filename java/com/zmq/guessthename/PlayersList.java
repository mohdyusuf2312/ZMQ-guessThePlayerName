package com.zmq.guessthename;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
public class PlayersList {
    private final String jsonStr;
    public PlayersList(Context context){
        this.jsonStr = loadJson(context);
    }

//    size of list
    public int size(){
        try {
            JSONObject rootObject = new JSONObject(jsonStr);
            JSONArray playersArray = rootObject.getJSONArray("players");
            return playersArray.length();
        } catch (JSONException err) {
            err.printStackTrace();
            return 0;
        }
    }

//    choose player
    public int chosenPlayerIndex(){
        Random random = new Random();
        return random.nextInt(size());
    }

    public Bitmap getPlayerImage(@NonNull Context context, int index){
        String imageName = chosenPlayerImageName(index);
        @SuppressLint("DiscouragedApi")
        int resId = context.getResources().getIdentifier(
                imageName,
                "raw",
                context.getPackageName()
        );
        if (resId == 0) {
            return BitmapFactory.decodeResource(context.getResources(), R.raw.default_player);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        if (bitmap == null) {
            return BitmapFactory.decodeResource(context.getResources(), R.raw.default_player);
        }
        return bitmap;
    }

    public String loadJson(Context context) {
        String json = null;
        try {
            InputStream iStream = context.getResources().openRawResource(R.raw.players);
            int size = iStream.available();
            byte[] buffer = new byte[size];
            iStream.read(buffer);
            iStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException err) {
            err.printStackTrace();
            return "JSON file not found";
        }
        return json;
    }

    public String chosenPlayer(int index) {
        try {
            JSONObject data = getPlayerData(index);
            return data.getString("name");
        } catch (JSONException err) {
            err.printStackTrace();
            return "Player name not found.";
        }
    }

    public String chosenPlayerImageName(int index) {
        try {
            JSONObject data = getPlayerData(index);
            return data.getString("imagePath");
        } catch (JSONException err) {
            err.printStackTrace();
            return "Player imagePath not found.";
        }
    }

    public String getHint(int index) {
        try {
            JSONObject data = getPlayerData(index);
            JSONArray playerHints = data.getJSONArray("hints");
            Random random = new Random();
            return "Hint: " + playerHints.getString(random.nextInt(playerHints.length()));
        } catch (JSONException err) {
            err.printStackTrace();
            return "Hint not found.";
        }
    }

    public JSONObject getPlayerData(int index) {
        try {
            JSONObject rootObj = new JSONObject(jsonStr);
            JSONArray playersArray = rootObj.getJSONArray("players");
            return playersArray.getJSONObject(index);
        } catch (JSONException err) {
            err.printStackTrace();
            return null;
        }
    }
}

