package org.ititandev.instagram.Login;

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

import com.google.firebase.auth.FirebaseAuth;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.ititandev.instagram.Home.HomeActivity;
import org.ititandev.instagram.R;
import org.ititandev.instagram.service.HttpService;

import cz.msebera.android.httpclient.Header;

/**
 * Created by User on 6/19/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final Boolean CHECK_IF_VERIFIED = false;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = LoginActivity.this;
        Log.d(TAG, "onCreate: started.");

        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        //setupAuth();
        init();

    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string if null.");

        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }

     /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    private void init() {

        //initialize the button for logging in
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to log in.");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) && isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    login(email, password);

//                    mAuth.signInWithEmailAndPassword(email, password)
//                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
//                                    FirebaseUser user = mAuth.getCurrentUser();
//
//                                    // If sign in fails, display a message to the user. If sign in succeeds
//                                    // the auth state listener will be notified and logic to handle the
//                                    // signed in user can be handled in the listener.
//                                    if (!task.isSuccessful()) {
//                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
//
//                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed),
//                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        mPleaseWait.setVisibility(View.GONE);
//                                    } else {
//                                        try {
//                                            if (CHECK_IF_VERIFIED) {
//                                                if (user.isEmailVerified()) {
//                                                    Log.d(TAG, "onComplete: success. email is verified.");
//                                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                                    startActivity(intent);
//                                                } else {
//                                                    Toast.makeText(mContext, "Email is not verified \n check your email inbox.", Toast.LENGTH_SHORT).show();
//                                                    mProgressBar.setVisibility(View.GONE);
//                                                    mPleaseWait.setVisibility(View.GONE);
//                                                    mAuth.signOut();
//                                                }
//                                            } else {
//                                                Log.d(TAG, "onComplete: success. email is verified.");
//                                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                                                startActivity(intent);
//                                            }
//
//                                        } catch (NullPointerException e) {
//                                            Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
//                                        }
//                                    }
//
//                                    // ...
//                                }
//                            });


                }

            }
        });


        //SignUp
        TextView linkSignUp = (TextView) findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Nếu có người đã đăng nhập, refresh token và chuyển home
//        if (mAuth.getCurrentUser() != null) {
//        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//        startActivity(intent);
//        finish();
//        }
    }

    /**
     * Setup the firebase auth object
     */
    private void setupAuth() {
        Log.d(TAG, "setupAuth: setting up firebase auth.");
//        SharedPreferences sharedPreferences = getSharedPreferences("ititandev", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        sharedPreferences.getBoolean("logined", false);
//        sharedPreferences.getString("username", "");
//        sharedPreferences.getString("token", "");

    }

    @Override
    public void onStart() {
        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void login(String username, String password) {
        String requestLogin = "{\"username\": \"" + username + "\",\"password\": \"" + password + "\"}";
        HttpService.post_text("login", requestLogin, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.v("Auth Token", headers[8].toString().substring(15, headers[8].toString().length()));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Auth Token failure", String.valueOf(statusCode));
            }
        });
    }
}
























