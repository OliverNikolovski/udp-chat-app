package server;

import server.exception.InvalidUsernameException;
import server.exception.UsernameTakenException;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class ChatServer {

    private final HashMap<String, SocketAddress> clients;
    private final DatagramSocket socket;

    public ChatServer(InetAddress address, int port) throws SocketException {
        this.socket = new DatagramSocket(port, address);
        this.clients = new HashMap<>();
    }

    public void startServer() {
        System.out.println("Server up and running...");
        try {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                new Thread(new MessageHandler(packet, this)).start();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public void addClient(String username, SocketAddress socketAddress) throws InvalidUsernameException, UsernameTakenException {
        synchronized (clients) {
            validateUsername(username);
            clients.put(username, socketAddress);
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

    public SocketAddress getSocketAddressForUser(String username) {
        return this.clients.get(username);
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public HashMap<String, SocketAddress> getClients() {
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

    public static void main(String[] args) throws SocketException, UnknownHostException {
        var address = InetAddress.getByName("localhost");
        var port = 4567;
        var chatServer = new ChatServer(address, port);
        chatServer.startServer();
    }
}
