package server;

import java.io.*;
import java.util.Objects;

public class ClientInfo {
    private final String username;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;

    public ClientInfo(String username, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(objectInputStream);
        Objects.requireNonNull(objectOutputStream);
        this.username = username;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public String getUsername() {
        return username;
    }

    public void closeSocket() {
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
    }
}
