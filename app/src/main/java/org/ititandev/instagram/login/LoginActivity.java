package org.ititandev.instagram.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.ititandev.instagram.R;
import org.ititandev.instagram.home.HomeActivity;
import org.ititandev.instagram.service.HttpService;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mUsername, mPassword;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("instagram", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String username = sharedPreferences.getString("username", "");
        String token = sharedPreferences.getString("token", "");
        if (!username.equals("")) {
            Log.v(TAG, "find existed username: " + username);
            Log.v(TAG, "find existed token: " + token);
            refreshToken(username, token);
        } else {
            Log.v(TAG, "No existed user");
        }


        setContentView(R.layout.activity_login);
        mProgressBar = findViewById(R.id.progressBar);
        mUsername = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = LoginActivity.this;
        Log.d(TAG, "onCreate: started.");

        mProgressBar.setVisibility(View.GONE);
        init();

    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            Log.v(TAG, "intent: " + intent.getStringExtra("username"));
            Log.v(TAG, "intent: " + intent.getStringExtra("password"));
            Log.v(TAG, "intent: " + intent.getStringExtra("message"));
            mUsername.setText(intent.getStringExtra("username"));
            mPassword.setText(intent.getStringExtra("password"));
            Toast.makeText(mContext, intent.getStringExtra("message"), Toast.LENGTH_LONG).show();
        }
    }


    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string if null.");
        return string.equals("");
    }

    private void init() {
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in.");

                String email = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) && isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_LONG).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    login(email, password);
                }

            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Nếu có người đã đăng nhập, refresh token và chuyển home
//        if (mAuth.getCurrentUser() != null) {
//        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//        startActivity(intent);
//        finish();
//        }
    }

    public void login(final String username, String password) {
        String requestLogin = "{\"username\": \"" + username + "\",\"password\": \"" + password + "\"}";
        HttpService.postBody("/login", requestLogin, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                mProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Auth Token failure :" + String.valueOf(statusCode));
                Log.e(TAG, "errorResponse :" + errorResponse.toString());
                try {
                    Toast.makeText(mContext, errorResponse.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                mProgressBar.setVisibility(View.GONE);
                Log.v(TAG, "jsonResponse :" + jsonResponse);
                Log.v(TAG, "statusCode :" + String.valueOf(statusCode));
                if (statusCode == 200) {
                    if (Boolean.valueOf(jsonResponse)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        try {
                            for (int i = 0; i < 15; i++) {
                                if (headers[i].toString().contains("Authorization")) {
                                    editor.putString("token", headers[i].toString().substring(15, headers[i].toString().length()));
                                    Log.v(TAG, "Auth Token success :" + headers[i].toString().substring(15, headers[i].toString().length()));
                                    break;
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        editor.commit();
                        Log.d(TAG, "Account logined and verified");
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(mContext, "Email is not verified\nCheck your email inbox", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "Auth Token failure :" + String.valueOf(statusCode));
                    Log.e(TAG, "jsonResponse :" + jsonResponse);
                }
            }
        });
    }

    private void refreshToken(final String username, final String token) {

        HttpService.postHeader("/refresh", token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.v(TAG, "onSuccess: refresh token");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                mProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "onFailure: Refresh token statusCode :" + String.valueOf(statusCode));
                Log.e(TAG, "onFailure: errorResponse :" + errorResponse.toString());
                Log.e(TAG, "token: " + token);
                editor.clear();
                editor.commit();
                mUsername.setText(username);
                try {
                    Toast.makeText(mContext, errorResponse.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                mProgressBar.setVisibility(View.GONE);
                Log.v(TAG, "onFailure: String jsonResponse :" + jsonResponse);
                Log.v(TAG, "onFailure: statusCode :" + String.valueOf(statusCode));
                if (statusCode == 200) {
                    if (Boolean.valueOf(jsonResponse)) {
                        editor.clear();
                        editor.putString("username", username);
                        try {
                            for (int i = 0; i < 15; i++) {
                                if (headers[i].toString().contains("Authorization")) {
                                    editor.putString("token", headers[i].toString().substring(15, headers[i].toString().length()));
                                    Log.v(TAG, "Refresh Token success :" + headers[i].toString().substring(15, headers[i].toString().length()));
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        editor.commit();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(mContext, "Email is not verified\nCheck your email inbox", Toast.LENGTH_LONG).show();
                        editor.clear();
                        editor.commit();
                    }
                } else {
                    Log.e(TAG, "onFailure: Refresh Token failure :" + String.valueOf(statusCode));
                    Log.e(TAG, "onFailure: jsonResponse :" + jsonResponse);

                }
                mUsername.setText(username);
            }
        });
    }
}


/*
Intent intent = new Intent(mContext, LoginActivity.class);
startActivity(intent);
*/






















