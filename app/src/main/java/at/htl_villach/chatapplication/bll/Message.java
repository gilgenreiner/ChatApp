package at.htl_villach.chatapplication.bll;

/**
 * Created by pupil on 4/22/19.
 */

public class Message {
    private String sender;
    private String chatid;
    private String message;
    private Long timestamp;

    public Message(String sender, String chatid, String message, Long timestamp) {
        this.sender = sender;
        this.chatid = chatid;
        this.message = message;
        this.timestamp = timestamp;
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

    public String getChatid() {
        return chatid;
    }

    public void setChatid(String chatid) {
        this.chatid = chatid;
    }
}
