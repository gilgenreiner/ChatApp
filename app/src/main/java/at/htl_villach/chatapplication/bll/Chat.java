package at.htl_villach.chatapplication.bll;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pupil on 4/22/19.
 */

public class Chat implements Parcelable {

    private String id;
    private HashMap<String, String> users;

    public Chat(String id, HashMap<String, String> users) {
        this.id = id;
        this.users = users;
    }

    protected Chat(Parcel in) {
        id = in.readString();
        users = in.readHashMap(String.class.getClassLoader());
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeMap(users);
    }
}
