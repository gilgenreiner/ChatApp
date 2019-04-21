package at.htl_villach.chatapplication.bll;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private int profilePictureResource;
    private String name;
    private String email;
    private String userName;
    private String password;
    private String state;

    public User() {

    }
    public User(String name, String email, int profilePictureResource, String userName, String state, String password) {
        this.name = name;
        this.state = state;
        this.email = email;
        this.userName = userName;
        this.profilePictureResource = profilePictureResource;
        this.password = password;
    }

    protected User(Parcel in) {
        profilePictureResource = in.readInt();
        name = in.readString();
        email = in.readString();
        userName = in.readString();
        state = in.readString();
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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public int getProfilePicture() {
        return profilePictureResource;
    }

    public String getState() {
        return state;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(profilePictureResource);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(userName);
        dest.writeString(state);
    }
}
