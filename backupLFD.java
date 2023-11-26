
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import util.utilFunc;


public class backupLFD {
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

    private static int serverNumber = 1;
    static boolean stopTimer = false;
    public static int num = serverNumber; 

    //Variables that are needs to change manually:
    static String GDFAddress = "192.168.1.3";
    
    private static int portGDF = 9886;

    private static int backupServerPort = 9916; 


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

        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), backupLFD.backupServerPort);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject("heartBeat");

        System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (backupLFD.num + 1) + " sending heartbeat to S" + (backupLFD.num + 1) + ANSI_RESET);
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + heartBeatCount + " backupLFD " + (backupLFD.num + 1) + " receives heartbeat from S" + (backupLFD.num + 1) + ANSI_RESET);
        }
    }

        public static void sendHeartBeatToGFD(boolean serverReachable) throws IOException, ClassNotFoundException{
            Socket socket = new Socket(GDFAddress, portGDF);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
    
            if (serverReachable) {
                String msg = String.format("LFD%d: add replica S%d", backupLFD.num + 1, backupLFD.num + 1);
                outputStream.writeObject(msg);
                System.out.println("[" + utilFunc.getTime() + "] " + msg);
            }
            else {
                String msg = String.format("LFD%d: delete replica S%d", backupLFD.num + 1, backupLFD.num + 1);
                //String msg = "LFD1: delete replica S1";
                outputStream.writeObject(msg);
                System.out.println("[" + utilFunc.getTime() + "] " + msg);
            }
        
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            String inputMessage = (String)inputStream.readObject();
    
            //Verify if the server successfully receives the heartbeat message
            if(inputMessage.equals("heartbeat message received")){
                System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + " LFD" + (backupLFD.num + 1) + "'s heartbeat received by GFD" + ANSI_RESET);
            }
        }
    }
