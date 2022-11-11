package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientPacketReceiver extends Thread {

    private final DatagramSocket socket;
    private final ThreadManager threadManager;

    public ClientPacketReceiver(DatagramSocket socket, ThreadManager threadManager) {
        this.socket = socket;
        this.threadManager = threadManager;
    }

    @Override
    public void run() {
        System.out.println("Client up and running...");
        byte[] buf = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        try {
            while (!isInterrupted()) {
                socket.receive(datagramPacket);
                byte[] data = datagramPacket.getData();
                String s = new String(data, 0, datagramPacket.getLength());
                String message = s;
                if (s.startsWith("message:")) {
                    String[] parts = s.split(":");
                    String fromUser = parts[1];
                    message = fromUser + ": " + parts[2];
                }
                System.out.println(message);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            this.threadManager.stopThreads();
        }
    }
}
