package org.ititandev.instagram.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.ititandev.instagram.home.HomeActivity;
import org.ititandev.instagram.login.LoginActivity;
import org.ititandev.instagram.profile.ProfileActivity;
import org.ititandev.instagram.R;
import org.ititandev.instagram.service.HttpService;
import org.ititandev.instagram.util.BottomNavigationViewHelper;
import org.ititandev.instagram.util.UserListAdapter;
import org.ititandev.instagram.models.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by User on 5/28/2017.
 */

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM = 1;

    private Context mContext = SearchActivity.this;

    //widgets
    private EditText mSearchParam;
    private ListView mListView;

    //vars
    private List<User> mUserList;
    private UserListAdapter mAdapter;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listView);
        sharedPreferences = getSharedPreferences("instagram", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.d(TAG, "onCreate: started.");

        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");
        mUserList = new ArrayList<>();
        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        //update the users list view
        if (keyword.length() == 0) {
            return;
        } else {
            String token = sharedPreferences.getString("token", "");
            HttpService.getHeader("/search/user/" + keyword, token, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Log.v(TAG, "response.length() = " + response.length());
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            Log.v(TAG, response.get(i).toString());
                            JSONObject json = response.getJSONObject(i);
                            User temp = new User();
                            temp.setUser_id("0");
                            temp.setUsername(json.getString("username"));
                            temp.setEmail(json.getString("email"));
                            temp.setAvatar_filename(json.getString("avatar_filename"));
                            temp.setPhone_number(0);
                            mUserList.add(temp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    updateUsersList();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG, "onFailure: statusCode: " + String.valueOf(statusCode));
                    Log.e(TAG, "onFailure: errorResponse :" + errorResponse.toString());
                    if (statusCode == 401) {
                        editor.clear();
                        editor.commit();
                        try {
                            Toast.makeText(mContext, errorResponse.get("message").toString(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SearchActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                    Log.v(TAG, "onFailure: String jsonResponse :" + jsonResponse);
                    Log.v(TAG, "onFailure: statusCode :" + String.valueOf(statusCode));
                    Toast.makeText(mContext, jsonResponse, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void updateUsersList() {
        Log.d(TAG, "updateUsersList: updating users list");

        mAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

                //navigate to profile activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), mUserList.get(position));
                startActivity(intent);
            }
        });
    }


    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
