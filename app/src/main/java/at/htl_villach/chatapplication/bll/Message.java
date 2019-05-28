package at.htl_villach.chatapplication.bll;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by pupil on 4/22/19.
 */

public class Message implements Parcelable {
    private String sender;
    private String id;
    private String message;
    private Long timestamp;
    private boolean isseen;

    public Message(String sender, String id, String message, Long timestamp, boolean isseen) {
        this.sender = sender;
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.isseen = isseen;
    }

    public Message() {
    }

    protected Message(Parcel in) {
        sender = in.readString();
        id = in.readString();
        message = in.readString();
        if (in.readByte() == 0) {
            timestamp = null;
        } else {
            timestamp = in.readLong();
        }
        isseen = in.readByte() != 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeAsString() {
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.setTimeInMillis(this.timestamp * 1000L);
        return DateFormat.format("hh:mm", cal).toString();
    }

    public String getTimeAsDate() {
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.setTimeInMillis(this.timestamp * 1000L);
        return DateFormat.format("dd. MMMM yyyy", cal).toString();
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeString(id);
        dest.writeString(message);
        if (timestamp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(timestamp);
        }
        dest.writeByte((byte) (isseen ? 1 : 0));
    }

    @Override
    public boolean equals(Object obj) {
        Message m = (Message) obj;

        return this.getId().equals(m.getId());
    }
}
