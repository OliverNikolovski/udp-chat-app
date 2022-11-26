package model;

import java.io.*;
import java.util.Objects;

public class Client implements Serializable {
    private String username;
    private transient ObjectOutputStream objectOutputStream;
    private transient ObjectInputStream objectInputStream;

    public Client() {}

    public Client(String username, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
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

    @Override
    public String toString() {
        return username;
    }
}
