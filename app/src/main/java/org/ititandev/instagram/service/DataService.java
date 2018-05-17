package org.ititandev.instagram.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class DataService extends Activity {
    SharedPreferences sharedPreferences = getSharedPreferences("instagram", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    public void setAuth(String username, String token) {
        editor.putString("username", username);
        editor.putString("token", token);
        editor.commit();
    }

    public String getUsername() {
        return sharedPreferences.getString("username", "");
    }

    public String getToken() {
        return sharedPreferences.getString("token", "");
    }
}
