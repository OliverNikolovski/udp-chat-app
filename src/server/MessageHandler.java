package server;

import server.exception.InvalidCommandException;
import server.exception.InvalidUsernameException;
import server.exception.UsernameTakenException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

public class MessageHandler implements Runnable {

    private static final String COMMAND_PARTS_SEPARATOR = ":";
    private static final byte[] SUCCESSFUL_LOGIN_MESSAGE =
            """
                    Login successful. You can now start chatting with other clients.
                    Use the 'list' command to see a list of all logged in clients.
                    Use the 'message:<name-to>:<the-message>' command to send a message to a particular client.""".getBytes();
    private static final byte[] NOT_AUTHENTICATED_MESSAGE =
            """
                    You must first login to be able to interact with the server.
                    Command to login: login:<username>""".getBytes();
    private static final byte[] SUCCESSFUL_LOGOUT_MESSAGE = "You have successfully logged out from the chat.".getBytes();
    private static final String RECEIVING_USER_DOES_NOT_EXIST = "%s is not logged in.";

    private final byte[] data;
    private final int receivedDataLength;
    private final SocketAddress socketAddress;
    private final ChatServer chatServer;

    public MessageHandler(DatagramPacket receivedPacket, ChatServer chatServer) {
        this.data = receivedPacket.getData();
        this.receivedDataLength = receivedPacket.getLength();
        this.socketAddress = receivedPacket.getSocketAddress();
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        String command = new String(this.data, 0, this.receivedDataLength);
        System.out.println("Received command: " + command);
        try {
            Command commandType = getCommandType(command);
            switch (commandType) {
                case LOGIN -> handleLoginCommand(command);
                case LIST -> handleListCommand(command);
                case SEND_MESSAGE -> handleSendMessageCommand(command);
                case EXIT -> handleExitCommand(command);
            }
        }
        catch (Exception e) {
            this.sendErrorMessageToSocketAddress(e.getMessage(), this.socketAddress);
        }
    }

    private void handleLoginCommand(String command) throws InvalidUsernameException, UsernameTakenException, IOException {
        String[] parts = this.splitCommand(command);
        this.chatServer.addClient(parts[1], this.socketAddress);
        var datagramPacket = new DatagramPacket(SUCCESSFUL_LOGIN_MESSAGE, SUCCESSFUL_LOGIN_MESSAGE.length, this.socketAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void handleListCommand(String command) throws IOException {
        String username = this.splitCommand(command)[0];
        DatagramPacket datagramPacket;
        if (!this.chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        String loggedInUsers = String.join(", ", this.chatServer.getClients().keySet());
        datagramPacket = new DatagramPacket(loggedInUsers.getBytes(), loggedInUsers.length(), this.socketAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void handleSendMessageCommand(String command) throws IOException {
        String[] parts = this.splitCommand(command);
        String fromUser = parts[0];
        String toUser = parts[2];
        String message = parts[3];
        if (!this.chatServer.isLoggedIn(fromUser)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        if (!this.chatServer.isLoggedIn(toUser)) {
            this.sendMessageForNotLoggedInReceivingUser(toUser);
            return;
        }
        SocketAddress receivingUserAddress = this.chatServer.getSocketAddressForUser(toUser);
        String response = "message:" + fromUser + ":" + message;
        byte[] buf = response.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, receivingUserAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void handleExitCommand(String command) throws IOException {
        String username = this.splitCommand(command)[0];
        if (!chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        this.chatServer.removeClient(username);
        DatagramPacket datagramPacket =
                new DatagramPacket(SUCCESSFUL_LOGOUT_MESSAGE, SUCCESSFUL_LOGOUT_MESSAGE.length, this.socketAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void sendMessageForUnauthenticatedUser() throws IOException {
        DatagramPacket datagramPacket =
                new DatagramPacket(NOT_AUTHENTICATED_MESSAGE, NOT_AUTHENTICATED_MESSAGE.length, this.socketAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void sendMessageForNotLoggedInReceivingUser(String username) throws IOException {
        byte[] buf = String.format(RECEIVING_USER_DOES_NOT_EXIST, username).getBytes();
        DatagramPacket datagramPacket =
                new DatagramPacket(buf, buf.length, this.socketAddress);
        this.chatServer.getSocket().send(datagramPacket);
    }

    private void sendErrorMessageToSocketAddress(String message, SocketAddress socketAddress) {
        byte[] buf = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socketAddress);
        try {
            this.chatServer.getSocket().send(datagramPacket);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] splitCommand(String command) {
        return command.split(COMMAND_PARTS_SEPARATOR);
    }

    private Command getCommandType(String command) throws InvalidCommandException {
        String[] parts = command.split(COMMAND_PARTS_SEPARATOR);
        if (parts[0].equals("login") && parts.length == 2)
            return Command.LOGIN;
        else if (parts[1].equals("list") && parts.length == 2)
            return Command.LIST;
        else if (parts[1].equals("message") && parts.length == 4)
            return Command.SEND_MESSAGE;
        else if (parts[1].equals("exit") && parts.length == 2)
            return Command.EXIT;
        else
            throw new InvalidCommandException("Invalid command");
    }
}
