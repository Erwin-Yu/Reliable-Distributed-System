import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GFD {
    // ANSI Color Codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    private static int memberCount = 0;
    private static List<String> membership = new ArrayList<>();
    private static int portRM = 10000;

    private static Map<String, Long> lastHeartbeatTime = new HashMap<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // CHANGE ME!!!
        InetAddress[] hosts = new InetAddress[]{InetAddress.getByName("172.26.26.131"),
                                                InetAddress.getByName("172.26.29.61"),
                                                InetAddress.getByName("172.26.95.8")};

        int port= 9886;
        int hbPort = 9896;

        // Create thread for sending HBs to LFDs
        Thread hbThread = sendHeartBeatToLFDs(hosts, hbPort);
        hbThread.start();
        
        // Create threads for each server socket
        Thread thread = createSocketThread(hosts, port);
        thread.start();
    
        System.out.println(ANSI_BLUE + "GFD: 0 members" + ANSI_RESET);
    }

    private static Thread createSocketThread(InetAddress[] hosts, int port) {
        return new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while (true) {
                    Socket socket = serverSocket.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                    String message = (String) inputStream.readObject();
                    System.out.println(message);
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
                    sendHeartBeatToRM(message);
                    sendHeartBeatToLFDs(hosts, port);
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
        System.out.println(ANSI_BLUE + msg + ANSI_RESET);

        socket.close();
    }

    public static void sendHeartBeatToRM(String msg) throws IOException, ClassNotFoundException{
        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), portRM);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        // System.out.println("msg is " + msg);
        outputStream.writeObject(msg);
    }

    public static Thread sendHeartBeatToLFDs(InetAddress[] hosts, int port) {
        return new Thread(() -> {
            while (true) {
                try {
                    System.out.println(ANSI_YELLOW + "GFD heartbeating LFDs" + ANSI_RESET);
                    
                    for (InetAddress host : hosts) {
                        Socket socket = new Socket();
                        // Set connection timeout to 2 seconds (2000 milliseconds)
                        socket.connect(new InetSocketAddress(host, port), 2000);
                        socket.setSoTimeout(5000);
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        // Send a heartbeat message
                        outputStream.writeObject("heartbeat");
                    }
                } catch (IOException e) {
                    // System.out.println("Failed to send heartbeat");
                    // e.printStackTrace();
                }

                // Wait 5s
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }
        });
    }
}
