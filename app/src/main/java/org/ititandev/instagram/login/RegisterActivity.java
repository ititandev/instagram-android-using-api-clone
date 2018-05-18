package org.ititandev.instagram.login;

import android.content.Context;
import android.content.Intent;
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
import org.ititandev.instagram.service.HttpService;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by User on 6/19/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, username, password, name;
    private EditText mEmail, mPassword, mUsername, mName;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        Log.d(TAG, "onCreate: started.");

        initWidgets();
        init();
    }

    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "onClick");
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                name = mName.getText().toString();

                if (checkInputs(email, username, password, name)) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);
                    signUp(username, password, email, name);
                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password, String name) {
        Log.d(TAG, "checkInputs: checking inputs for null values.");
        if (email.equals("") || username.equals("") || password.equals("") || name.equals("")) {
            Toast.makeText(mContext, "All fields must be filled out.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Initialize the activity widgets
     */
    private void initWidgets() {
        Log.d(TAG, "initWidgets: Initializing Widgets.");
        mEmail = findViewById(R.id.input_email);
        mUsername = findViewById(R.id.input_username);
        mName = findViewById(R.id.input_name);
        btnRegister = findViewById(R.id.btn_register);
        mProgressBar = findViewById(R.id.progressBar);
        loadingPleaseWait = findViewById(R.id.loadingPleaseWait);
        mPassword = findViewById(R.id.input_password);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }

    public void signUp(final String username, final String password, String email, String name) {
        String requestSignup = "{\"username\": \"" + username + "\",\"password\": \"" + password + "\",\"email\": \"" + email + "\",\"name\": \"" + name + "\"}";
        HttpService.postBody("/signup", requestSignup, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                mProgressBar.setVisibility(View.GONE);
                loadingPleaseWait.setVisibility(View.GONE);
                Log.e(TAG, "statusCode :" + String.valueOf(statusCode));
                Log.e(TAG, "errorResponse :" + errorResponse.toString());
                try {
                    Toast.makeText(mContext, errorResponse.get("message").toString().trim(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                mProgressBar.setVisibility(View.GONE);
                loadingPleaseWait.setVisibility(View.GONE);
                if (statusCode == 200) {
                    Log.v(TAG, "jsonResponse: " + jsonResponse);
                    Log.v(TAG, "statusCode: " + String.valueOf(statusCode));
                    if (Boolean.valueOf(jsonResponse)) {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        intent.putExtra("message", "Register successfully.\nCheck email inbox to verify");
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Toast.makeText(mContext, jsonResponse, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG,"statusCode: " + String.valueOf(statusCode));
                    Log.e(TAG,"jsonResponse: " + jsonResponse);
                }
            }
        });
    }
}
