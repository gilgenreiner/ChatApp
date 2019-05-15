package at.htl_villach.chatapplication.bll;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by pupil on 4/22/19.
 */

public class Message {
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
}
