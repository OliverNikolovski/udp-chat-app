package server;

import model.Client;
import server.exception.InvalidUsernameException;
import server.exception.UsernameTakenException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;

public class ChatServer {

    private final HashMap<String, Client> clients;
    private final ServerSocket serverSocket;

    public ChatServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.clients = new HashMap<>();
    }

    public void startServer() {
        System.out.println("Server up and running...");
        try {
            while (true) {
                var socket = this.serverSocket.accept();
                System.out.println("Client has connected to the server.");
                new Thread(new ClientHandler(socket, this)).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public Client addClient(String username, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream)
            throws InvalidUsernameException, UsernameTakenException, IOException {
        synchronized (clients) {
            validateUsername(username);
            Client client = new Client(username, objectInputStream, objectOutputStream);
            return clients.put(username, client);
        }
    }

    public void removeClient(String username) {
        synchronized (clients) {
            this.clients.remove(username);
        }
    }

    public boolean isLoggedIn(String username) {
        return this.clients.containsKey(username);
    }

    public HashMap<String, Client> getClients() {
        return clients;
    }

    private void validateUsername(String username) throws InvalidUsernameException, UsernameTakenException {
        if (isUsernameInvalid(username))
            throw new InvalidUsernameException("Username must be at least 6 characters long.");
        if (isUsernameTaken(username))
            throw new UsernameTakenException("Username " + username + " is taken. Please choose a different one.");
    }

    private boolean isUsernameInvalid(String username) {
        return username.length() < 6;
    }

    private boolean isUsernameTaken(String username) {
        return this.clients.containsKey(username);
    }

    public static void main(String[] args) throws IOException {
        var port = 4567;
        var chatServer = new ChatServer(port);
        chatServer.startServer();
    }
}
