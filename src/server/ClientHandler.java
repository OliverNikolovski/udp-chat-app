package server;

import server.exception.InvalidCommandException;
import server.exception.InvalidUsernameException;
import server.exception.UsernameTakenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;
    private final ChatServer chatServer;
    private ClientInfo client;

    public ClientHandler(Socket socket, ChatServer chatServer) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printWriter = new PrintWriter(socket.getOutputStream());
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        String command;
        try {
            while ((command = bufferedReader.readLine()) != null) {
                Command commandType = getCommandType(command);
                switch (commandType) {
                    case LOGIN -> handleLoginCommand(command);
                    case LIST -> handleListCommand(command);
                    case SEND_MESSAGE -> handleSendMessageCommand(command);
                    case EXIT -> handleExitCommand(command);
                }
            }
        }
        catch (Exception ex) {
            this.sendErrorMessage(ex.getMessage());
            this.closeEverything();
        }
    }

    private void handleLoginCommand(String command) throws InvalidUsernameException, UsernameTakenException, IOException {
        String[] parts = this.splitCommand(command);
        this.client = this.chatServer.addClient(parts[1], this.socket);
        this.printWriter.println(SUCCESSFUL_LOGIN_MESSAGE);
        this.printWriter.flush();
    }

    private void handleListCommand(String command) {
        String username = this.splitCommand(command)[0];
        if (!this.chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        String loggedInUsers = String.join(", ", this.chatServer.getClients().keySet());
        this.printWriter.println(loggedInUsers);
        this.printWriter.flush();
    }

    private void handleSendMessageCommand(String command) {
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
        ClientInfo receiver = this.chatServer.getClients().get(toUser);
        PrintWriter pw = receiver.getPrintWriter();
        String response = "message:" + fromUser + ":" + message;
        pw.println(response);
        pw.flush();
    }

    private void handleExitCommand(String command) {
        String username = this.splitCommand(command)[0];
        if (!chatServer.isLoggedIn(username)) {
            this.sendMessageForUnauthenticatedUser();
            return;
        }
        this.chatServer.removeClient(username);
        this.printWriter.println(SUCCESSFUL_LOGOUT_MESSAGE);
        this.printWriter.flush();
    }

    private void sendMessageForUnauthenticatedUser() {
        this.printWriter.println(NOT_AUTHENTICATED_MESSAGE);
        this.printWriter.flush();
    }

    private void sendMessageForNotLoggedInReceivingUser(String username) {
        this.printWriter.println(String.format(RECEIVING_USER_DOES_NOT_EXIST, username));
        this.printWriter.flush();
    }

    private void sendErrorMessage(String message) {
        this.printWriter.println(message);
        this.printWriter.flush();
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

    private void closeEverything() {
        printWriter.close();
        if (client != null)
            client.closeSocket();
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
