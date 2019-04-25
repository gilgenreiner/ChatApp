package at.htl_villach.chatapplication.bll;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, String> users) {
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

    public String getReceiver(String uid) {
        String result = "";
        for (Map.Entry<String, String> entry : users.entrySet()) {
            if(!uid.equals(entry.getKey())){
                result = entry.getKey();
            }
        }
        return result;
    }
}
