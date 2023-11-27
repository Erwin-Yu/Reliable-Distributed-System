import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RM {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private static int memberCount = 0;
    private static List<String> membership = new ArrayList<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port= 10000;
        
        // Create threads for each server socket
        Thread thread = createSocketThread(port);
        thread.start();
    
        System.out.println("RM: 0 members");
    }

private static Thread createSocketThread(int port) {
        return new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    String message = (String) inputStream.readObject();
                    System.out.println(message);
                    // System.out.println("hello");
                    String[] msgs = message.split(" ");
                    String msg = msgs[msgs.length - 1];
                    // System.out.println(message.split(" ")[1]);
                    if (message.split(" ")[1].equals("add")) {
                        synchronized (membership) {
                            memberCount++;
                            membership.add(msg);
                        }
                    } else {
                        synchronized (membership) {
                            memberCount--;
                            membership.remove(msg);
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

        String msg = "RM: ";
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
