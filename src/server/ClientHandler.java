package server;

import dto.Message;
import server.exception.InvalidCommandException;
import server.exception.InvalidUsernameException;
import server.exception.UsernameTakenException;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final String COMMAND_PARTS_SEPARATOR = ":";
    private static final String SUCCESSFUL_LOGIN_MESSAGE =
            """
                    Login successful. You can now start chatting with other clients.
                    Use the 'list' command to see a list of all logged in clients.
                    Use the 'message:<name-to>:<the-message>' command to send a message to a particular client.""";
    private static final String NOT_AUTHENTICATED_MESSAGE =
            """
                    You must first login to be able to interact with the server.
                    Command to login: login:<username>""";
    private static final String SUCCESSFUL_LOGOUT_MESSAGE = "You have successfully logged out from the chat.";
    private static final String RECEIVING_USER_DOES_NOT_EXIST = "%s is not logged in.";

    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final ChatServer chatServer;
    private ClientInfo client;

    public ClientHandler(Socket socket, ChatServer chatServer) throws IOException {
        this.socket = socket;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        try {
            while (!this.socket.isClosed()) {
                Message message = (Message) objectInputStream.readObject();
                Command commandType = getCommandType(message.getContent());
                switch (commandType) {
                    case LOGIN -> handleLoginCommand(message);
                    case LIST -> handleListCommand(message);
                    case SEND_MESSAGE -> handleSendMessageCommand(message);
                    case EXIT -> handleExitCommand(message);
                }
            }
        }
        catch (Exception ex) {
            try {
                this.sendErrorMessage(ex.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.closeEverything();
        }
    }

    private void handleLoginCommand(Message command) throws InvalidUsernameException, UsernameTakenException, IOException {
        String[] parts = this.splitCommand(command.getContent());
        this.client = this.chatServer.addClient(parts[1], this.objectInputStream, this.objectOutputStream);
        Message message = createMessage(SUCCESSFUL_LOGIN_MESSAGE);
        this.objectOutputStream.writeObject(message);
        this.objectOutputStream.flush();
    }

    private void handleListCommand(Message command) throws IOException {
        String username = command.getSenderUsername();
        if (!this.chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        String loggedInUsers = String.join(", ", this.chatServer.getClients().keySet());
        Message message = createMessage(loggedInUsers);
        this.objectOutputStream.writeObject(message);
        this.objectOutputStream.flush();
    }

    private void handleSendMessageCommand(Message command) throws IOException {
        String[] contentParts = this.splitCommand(command.getContent());
        String fromUser = command.getSenderUsername();
        String toUser = contentParts[1];
        String message = contentParts[2];
        if (!this.chatServer.isLoggedIn(fromUser)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        if (!this.chatServer.isLoggedIn(toUser)) {
            this.sendMessageForNotLoggedInReceivingUser(toUser);
            return;
        }
        ClientInfo receiver = this.chatServer.getClients().get(toUser);
        ObjectOutputStream objectOutputStream = receiver.getObjectOutputStream();
        objectOutputStream.writeObject(new Message(command.getSenderUsername(), message));
        objectOutputStream.flush();
    }

    private void handleExitCommand(Message command) throws IOException {
        String username = command.getSenderUsername();
        if (!chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        this.chatServer.removeClient(username);
        this.objectOutputStream.writeObject(createMessage(SUCCESSFUL_LOGOUT_MESSAGE));
        this.objectOutputStream.flush();
        this.closeEverything();
    }

    private void sendMessageForUnauthenticatedUser() throws IOException {
        this.objectOutputStream.writeObject(createMessage(NOT_AUTHENTICATED_MESSAGE));
        this.objectOutputStream.flush();
    }

    private void sendMessageForNotLoggedInReceivingUser(String username) throws IOException {
        String msg = String.format(RECEIVING_USER_DOES_NOT_EXIST, username);
        this.objectOutputStream.writeObject(createMessage(msg));
        this.objectOutputStream.flush();
    }

    private void sendErrorMessage(String message) throws IOException {
        this.objectOutputStream.writeObject(createMessage(message));
        this.objectOutputStream.flush();
    }

    private String[] splitCommand(String command) {
        return command.split(COMMAND_PARTS_SEPARATOR);
    }

    private Command getCommandType(String command) throws InvalidCommandException {
        String[] parts = command.split(COMMAND_PARTS_SEPARATOR);
        if (parts[0].equals("login") && parts.length == 2)
            return Command.LOGIN;
        else if (parts[0].equals("list") && parts.length == 1)
            return Command.LIST;
        else if (parts[0].equals("message") && parts.length == 3)
            return Command.SEND_MESSAGE;
        else if (parts[0].equals("exit") && parts.length == 1)
            return Command.EXIT;
        else
            throw new InvalidCommandException("Invalid command");
    }

    private Message createMessage(String content) {
        return new Message("Server", content);
    }

    private void closeEverything() {
        try {
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (client != null)
            client.closeSocket();
    }
}
