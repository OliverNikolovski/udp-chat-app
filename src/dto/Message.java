package dto;

public class Message {
    private String from;
    private String command;

    public Message(String from, String command) {
        this.from = from;
        this.command = command;
    }

    public String getFrom() {
        return from;
    }

    public String getCommand() {
        return command;
    }
}
