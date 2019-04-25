package at.htl_villach.chatapplication.bll;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String id;
    private String fullname;
    private int profilePictureResource;
    private String email;
    private String username;

    public User() {
        super();
        this.id = null;
        this.email = null;
        this.username = null;
        this.fullname = null;
        this.profilePictureResource = 0;
    }


    public User(String id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.profilePictureResource = 0;
    }

    protected User(Parcel in) {
        id = in.readString();
        profilePictureResource = in.readInt();
        email = in.readString();
        username = in.readString();
        fullname = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(profilePictureResource);
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(fullname);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) { this.username = username; }

    public int getProfilePicture() {
        return profilePictureResource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
