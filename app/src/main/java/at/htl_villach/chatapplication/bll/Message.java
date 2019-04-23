package at.htl_villach.chatapplication.bll;

/**
 * Created by pupil on 4/22/19.
 */

public class Message {
    private String sender;
    private String chatId;
    private String message;
    private String timestamp;

    public Message(String sender, String chatid, String message, String timestamp) {
        this.sender = sender;
        this.chatId = chatid;
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

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
