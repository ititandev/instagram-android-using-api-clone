package org.ititandev.instagram.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by User on 6/26/2017.
 */

public class User implements Parcelable {

    private String user_id;
    private long phone_number;
    private String email;
    private String username;
    private String avatar_filename;


    public User(String user_id, long phone_number, String email, String username, String avatar_filename) {
        this.user_id = user_id;
        this.phone_number = phone_number;
        this.email = email;
        this.username = username;
        this.avatar_filename = avatar_filename;
    }

    public User() {

    }

    protected User(Parcel in) {
        user_id = in.readString();
        phone_number = in.readLong();
        email = in.readString();
        username = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar_filename() {
        return avatar_filename;
    }

    public void setAvatar_filename(String avatar_filename) {
        this.avatar_filename = avatar_filename;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar_filename='" + avatar_filename + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeLong(phone_number);
        dest.writeString(email);
        dest.writeString(username);
    }
}
