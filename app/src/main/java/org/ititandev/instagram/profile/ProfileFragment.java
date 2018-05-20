package org.ititandev.instagram.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

import org.ititandev.instagram.BuildConfig;
import org.ititandev.instagram.R;
import org.ititandev.instagram.login.LoginActivity;
import org.ititandev.instagram.service.HttpService;
import org.ititandev.instagram.util.BottomNavigationViewHelper;
import org.ititandev.instagram.util.GridImageAdapter;
import org.ititandev.instagram.util.UniversalImageLoader;
import org.ititandev.instagram.models.Comment;
import org.ititandev.instagram.models.Like;
import org.ititandev.instagram.models.Photo;
import org.ititandev.instagram.models.UserAccountSettings;
import org.ititandev.instagram.models.UserSettings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";


    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;


    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<Photo> photos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        mProgressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolBar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();

        sharedPreferences = getActivity().getSharedPreferences("instagram", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.d(TAG, "onCreateView: stared.");

        setupBottomNavigationView();
        setupToolbar();
        setPhoto();
        setProfile();
        init(view);

        return view;
    }


    private void setPhoto() {
        Log.d(TAG, "setPhoto: Setting up image grid.");

        final ArrayList<Photo> photos = new ArrayList<>();
        String token = sharedPreferences.getString("token", "");
        String username = sharedPreferences.getString("username", "");
        HttpService.getHeader("/photo/" + username + "/0/45", token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                try {

                    Log.d(TAG, "onSuccess getPhoto: length: " + response.length());
                    for (int i = 0; i < response.length(); i++) {
                        Photo photo = new Photo();
                        JSONObject temp = response.getJSONObject(i);
                        photo.setCaption(temp.getString("caption"));
//                        photo.setTags(temp.getString(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(temp.getString("photo_id"));
                        photo.setUser_id(temp.getString("username"));
                        photo.setDate_created(temp.getString("datetime_upload"));
                        photo.setImage_path(temp.getString("filename"));

//                        ArrayList<Comment> comments = new ArrayList<Comment>();
//                        for (DataSnapshot dSnapshot : singleSnapshot
//                                .child(getString(R.string.field_comments)).getChildren()) {
//                            Comment comment = new Comment();
//                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//                            comments.add(comment);
//                        }
//
//                        photo.setComments(comments);
//
//                        List<Like> likesList = new ArrayList<Like>();
//                        for (DataSnapshot dSnapshot : singleSnapshot
//                                .child(getString(R.string.field_likes)).getChildren()) {
//                            Like like = new Like();
//                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//                            likesList.add(like);
//                        }
//                        photo.setLikes(likesList);

                        photos.add(photo);
                    }
                    setupImageGrid(photos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //setup our image grid

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "onFailure: statusCode: " + String.valueOf(statusCode));
                Log.e(TAG, "onFailure: errorResponse :" + errorResponse.toString());
                try {
                    Toast.makeText(mContext, errorResponse.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusCode == 401) {
                    editor.clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    editor.commit();
                    getActivity().finish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                Log.v(TAG, "onFailure: String jsonResponse :" + jsonResponse);
                Log.v(TAG, "onFailure: statusCode :" + String.valueOf(statusCode));
                Toast.makeText(mContext, jsonResponse, Toast.LENGTH_LONG).show();
                if (statusCode == 401) {
                    editor.clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    editor.commit();
                    getActivity().finish();
                }
            }
        });
    }

    private void setProfile() {
        String token = sharedPreferences.getString("token", "");
        String username = sharedPreferences.getString("username", "");

        HttpService.getHeader("/profile/" + username, token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    Log.v(TAG, "onSuccess: photo_num = " + response.toString());
                    mUsername.setText(response.getString("username"));
                    mDisplayName.setText(response.getString("name"));
                    mPosts.setText(response.getString("photo_num"));
                    mFollowers.setText(response.getString("follower_num"));
                    mFollowing.setText(response.getString("following_num"));
                    mWebsite.setText(response.getString("website"));
                    mDescription.setText(response.getString("biography"));
                    String imgURL = BuildConfig.SERVER_URL + "/download/avatar/" + response.getString("avatar_filename");
                    UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "onFailure: statusCode: " + String.valueOf(statusCode));
                Log.e(TAG, "onFailure: errorResponse :" + errorResponse.toString());
                try {
                    Toast.makeText(mContext, errorResponse.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (statusCode == 401) {
                    editor.clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    editor.commit();
                    getActivity().finish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String jsonResponse, Throwable throwable) {
                Log.v(TAG, "onFailure: String jsonResponse :" + jsonResponse);
                Log.v(TAG, "onFailure: statusCode :" + String.valueOf(statusCode));
                Toast.makeText(mContext, jsonResponse, Toast.LENGTH_LONG).show();
                if (statusCode == 401) {
                    editor.clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    editor.commit();
                    getActivity().finish();
                }
            }
        });
        mProgressBar.setVisibility(View.GONE);
    }

    private void setupImageGrid(final ArrayList<Photo> photos) {
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for (int i = 0; i < photos.size(); i++) {
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "",
                imgUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
            }
        });
    }

    private void init(View view) {
        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }


    private void setupToolbar() {

        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        super.onAttach(context);
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
