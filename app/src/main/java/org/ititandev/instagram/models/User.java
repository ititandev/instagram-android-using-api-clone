package org.ititandev.instagram.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by User on 6/26/2017.
 */

public class User implements Parcelable {


    private String name;
    private String email;
    private String username;
    private String avatar_filename;


    private String phone_number;


    public User(String name, String email, String username, String avatar_filename) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.avatar_filename = avatar_filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User() {

    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        username = in.readString();
        avatar_filename = in.readString();
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

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
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
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(avatar_filename);
    }
}
