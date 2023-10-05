import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GFD {
    private static int memberCount = 0;
    private static List<String> membership = new ArrayList<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port1 = 9886, port2 = 9887, port3 = 9888;
        
        // Create threads for each server socket
        Thread thread1 = createSocketThread(port1, "S1");
        Thread thread2 = createSocketThread(port2, "S2");
        Thread thread3 = createSocketThread(port3, "S3");

        thread1.start();
        thread2.start();
        thread3.start();
        
        System.out.println("GFD: 0 members");
    }

    private static Thread createSocketThread(int port, String replicaId) {
        return new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    String message = (String) inputStream.readObject();

                    if (message.split(" ")[1].equals("add")) {
                        synchronized (membership) {
                            memberCount++;
                            membership.add(replicaId);
                        }
                    } else {
                        synchronized (membership) {
                            memberCount--;
                            membership.remove(replicaId);
                        }
                    }
                    handleHeartbeat(socket);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private static void handleHeartbeat(Socket socket) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject("heartbeat message received");

        String msg = "GFD: ";
        msg += memberCount + " member" + (memberCount != 1 ? "s: " : ": ");
        
        synchronized (membership) {
            for (int i = 0; i < memberCount - 1; i++) {
                msg += membership.get(i) + ", ";
            }
            if (memberCount > 0) {
                msg += membership.get(memberCount - 1);
            }
        }
        System.out.println(msg);

        socket.close();
    }
}
