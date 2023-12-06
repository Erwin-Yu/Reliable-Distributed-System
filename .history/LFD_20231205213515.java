import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import util.utilFunc;



public class LFD {
    // ANSI Color Codes
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    static boolean stopTimer = false;
    public static int num = 1; 
    private static int portGDF = 9886;
    private static int port = 9876; 
    static String GDFAddress = "172.26.122.84";

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int heartBeatFreq = 1;
        Timer timer;
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        
        Scanner scanner = new Scanner(System.in);
        System.out.print(ANSI_RED + "Please input the heartBeatFreq:" + ANSI_RESET);
        String timeInterval = scanner.nextLine(); 
        int timeIntervalInt = Integer.parseInt(timeInterval);

        startHeartbeatListener(9896); // Listen to HB from GFD

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask(){

            int heartBeatCount = 0;
            int heartBeatCountGFD = 0;
            boolean serverReachable = false;

            @Override
            public void run(){

                try {
                    sendHeartBeatMessage(heartBeatCount);
                    if (!serverReachable) {
                        serverReachable = true;
                        try {
                            sendHeartBeatToGFD(serverReachable);
                        }
                        catch (ClassNotFoundException | IOException e) {}
                    }
                    heartBeatCount++;

                } catch (ClassNotFoundException | IOException e) {
                    if (serverReachable) {
                        serverReachable = false;
                        try {
                            sendHeartBeatToGFD(serverReachable);
                        }
                        catch (ClassNotFoundException | IOException ee) {}
                    }
                    System.out.println(ANSI_RED + "HeartBeat message failed... / Server is currently unreachable..." + ANSI_RESET);
                    //Terminate the heartbeat service
                    // timer.cancel();
                }
            }
        }, 0, timeIntervalInt);


    }


    public static void sendHeartBeatMessage(int heartBeatCount) throws IOException, ClassNotFoundException{

        //System.out.println("this is the server has port: " + (LFD.num + LFD.port));
        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), LFD.port);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject("heartBeat");

        System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (LFD.num + 1) + " sending heartbeat to S" + (LFD.num + 1) + ANSI_RESET);
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (LFD.num + 1) + " receives heartbeat from S" + (LFD.num + 1) + ANSI_RESET);
        }
    }

    public static void sendHeartBeatToGFD(boolean serverReachable) throws IOException, ClassNotFoundException{
        Socket socket = new Socket(GDFAddress, portGDF);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        if (serverReachable) {
            String msg = String.format("LFD%d: add replica S%d", LFD.num + 1, LFD.num + 1);
            outputStream.writeObject(msg);
            System.out.println("[" + utilFunc.getTime() + "] " + msg);
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
            System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + " LFD" + (LFD.num + 1) + "'s heartbeat received by GFD" + ANSI_RESET);
        }
    }

    private static void startHeartbeatListener(int port) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                        ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

                        String message = (String) inputStream.readObject();
                        if (message.equals("heartbeat")) {
                            System.out.println(ANSI_YELLOW + "Heartbeat received from GFD" + ANSI_RESET);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Error handling connection: " + e.getMessage());
                    }
                    System.out.println("fin");
                }
            } catch (IOException e) {
                System.out.println("Error starting server socket: " + e.getMessage());
            }
        }).start();
    }
}
