package org.ititandev.instagram.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.auth.FirebaseUser;
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
import org.ititandev.instagram.login.LoginActivity;
import org.ititandev.instagram.profile.AccountSettingsActivity;
import org.ititandev.instagram.R;
import org.ititandev.instagram.models.Comment;
import org.ititandev.instagram.models.Like;
import org.ititandev.instagram.models.Photo;
import org.ititandev.instagram.models.User;
import org.ititandev.instagram.models.UserAccountSettings;
import org.ititandev.instagram.models.UserSettings;
import org.ititandev.instagram.search.SearchActivity;
import org.ititandev.instagram.service.HttpService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ViewProfileFragment extends Fragment {

    private static final String TAG = "ViewProfileFragment";


    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription,
            mFollow, mUnfollow;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private ImageView mBackArrow;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;
    private TextView editProfile;

    private User mUser;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayList<Photo> photos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
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
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mFollow = view.findViewById(R.id.follow);
        mUnfollow = view.findViewById(R.id.unfollow);
        editProfile = view.findViewById(R.id.textEditProfile);
        mBackArrow = view.findViewById(R.id.backArrow);
        mContext = getActivity();

        sharedPreferences = getActivity().getSharedPreferences("instagram", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.d(TAG, "onCreateView: stared.");

        try {
            mUser = getUserFromBundle();
            setProfile();
            setPhoto();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
            Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }

        setupBottomNavigationView();
        init();

        return view;
    }

    private void onClickFollow() {
        String token = sharedPreferences.getString("token", "");
        HttpService.getHeader("/follow/" + mUser.getUsername(), token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    Log.v(TAG, "onSuccess: response: " + response.toString());
                    if (response.getBoolean("follow_status")) {
                        setFollowing();
                        mFollowers.setText(String.valueOf(Integer.valueOf(mFollowers.getText().toString()) + 1));
                    } else {
                        setUnfollowing();
                        mFollowers.setText(String.valueOf(Integer.valueOf(mFollowers.getText().toString()) - 1));
                    }
                } catch (Exception e) {
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
                Log.e(TAG, "onFailure: String jsonResponse :" + jsonResponse);
                Log.e(TAG, "onFailure: statusCode :" + String.valueOf(statusCode));
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
        mDisplayName.setText(mUser.getName());
        mUsername.setText(mUser.getUsername());
        String token = sharedPreferences.getString("token", "");
        HttpService.getHeader("/profile/" + mUser.getUsername(), token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    Log.v(TAG, "onSuccess: photo_num = " + response.toString());
                    mPosts.setText(response.getString("photo_num"));
                    mFollowers.setText(response.getString("follower_num"));
                    mFollowing.setText(response.getString("following_num"));

                    mWebsite.setText(response.getString("website"));
                    mDescription.setText(response.getString("biography"));

                    if (response.getBoolean("following"))
                        setFollowing();
                    else
                        setUnfollowing();
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
        String imgURL = BuildConfig.SERVER_URL + "/download/avatar/" + mUser.getAvatar_filename();
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "");
        mProgressBar.setVisibility(View.GONE);
    }


    private void init() {
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().finish();
            }
        });
        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now following: " + mUser.getUsername());
                onClickFollow();
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowing: " + mUser.getUsername());
                onClickFollow();
            }
        });
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

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);
    }

    private void setUnfollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        editProfile.setVisibility(View.GONE);
    }


    private User getUserFromBundle() {
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
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


    private void setPhoto() {

        photos = new ArrayList<Photo>();
        String token = sharedPreferences.getString("token", "");
        HttpService.getHeader("/photo/" + mUser.getUsername() + "/0/100", token, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                try {

                    Log.e(TAG, "onSuccess getPhoto: length: " + response.length());
                    for (int i = 0; i < response.length(); i++) {
                        Photo photo = new Photo();
                        JSONObject temp = response.getJSONObject(i);
                        photo.setImage_path(temp.getString("filename"));
                        photo.setCaption(temp.getString("caption"));
                        photo.setPhoto_id(temp.getString("photo_id"));
                        photo.setUser_id(temp.getString("username"));
                        photo.setDate_created(temp.getString("datetime_upload"));
                        photo.setComment_count(temp.getInt("comment_num"));
                        photo.setLike_count(temp.getInt("like_num"));
                        photo.setAvatar_filename(temp.getString("avatar_filename"));
                        photo.setName(temp.getString("name"));
                        photos.add(photo);
                        Log.e(TAG, "onSuccess getPhoto: response: " + temp.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                setupImageGrid(photos);

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


//        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
//        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
//        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
//        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
//        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
//        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());
//
//        ArrayList<Comment> comments = new ArrayList<Comment>();
//        for (DataSnapshot dSnapshot : singleSnapshot
//                .child(getString(R.string.field_comments)).getChildren()) {
//            Comment comment = new Comment();
//            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
//            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
//            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
//            comments.add(comment);
//        }
//
//        photo.setComments(comments);
//
//        List<Like> likesList = new ArrayList<Like>();
//        for (DataSnapshot dSnapshot : singleSnapshot
//                .child(getString(R.string.field_likes)).getChildren()) {
//            Like like = new Like();
//            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
//            likesList.add(like);
//        }
//        photo.setLikes(likesList);


    }

    private void setupImageGrid(final ArrayList<Photo> photos) {
        //setup our image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for (int i = 0; i < photos.size(); i++) {
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview, "", imgUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
            }
        });
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
