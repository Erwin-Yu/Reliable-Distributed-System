import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GFD {
    private static int memberCount = 0;
    private static List<String> membership = new ArrayList<>();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port= 9886;
        int portGF = 10000;
        // port2 = 9887, port3 = 9888;
        
        // Create threads for each server socket
        Thread thread = createSocketThread(port);
        thread.start();
    
        System.out.println("GFD: 0 members");
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

    public static void sendHeartBeatToRM(boolean serverReachable) throws IOException, ClassNotFoundException{
            Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), portRM);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
    
            if (serverReachable) {
                String msg = String.format("LFD%d: add replica S%d", LFD.num + 1, LFD.num + 1);
                outputStream.writeObject(msg);
            }
            else {
                String msg = String.format("LFD%d: delete replica S%d", LFD.num + 1, LFD.num + 1);
                //String msg = "LFD1: delete replica S1";
                outputStream.writeObject(msg);
                System.out.println("[" + utilFunc.getTime() + "] " + msg);
            }
        
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            String inputMessage = (String)inputStream.readObject();
    
            //Verify if the server successfully receives the heartbeat message
            if(inputMessage.equals("heartbeat message received")){
                System.out.println("[" + utilFunc.getTime() + "] " + " LFD" + (LFD.num + 1) + "'s heartbeat received by GFD");
            }
        }
}
