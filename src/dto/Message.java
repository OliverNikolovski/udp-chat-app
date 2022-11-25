package dto;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private String senderUsername;
    private String content;

    public Message() {}

    public Message(String senderUsername, String content) {
        Objects.requireNonNull(content);
        this.senderUsername = senderUsername;
        this.content = content;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(senderUsername, message.senderUsername) && content.equals(message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderUsername, content);
    }

    @Override
    public String toString() {
        return senderUsername + ": " + content;
    }
}
