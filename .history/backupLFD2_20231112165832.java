
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import util.utilFunc;


public class backupLFD2 {
    
    private static int serverNumber = 2;
    static boolean stopTimer = false;
    public static int num = serverNumber; 

    //Variables that are needs to change manually:
    // static String GDFAddress = "192.168.1.3";
    
    private static int portGDF = 9886;
    private static int backupServerPort = 9946; 


    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int heartBeatFreq = 1;
        Timer timer;
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please input the heartBeatFreq:");
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
                    System.out.println("HeartBeat message failed... / Server is currently unreachable...");
                    //Terminate the heartbeat service
                    // timer.cancel();
                }
            }
        }, 0, timeIntervalInt);


    }


    public static void sendHeartBeatMessage(int heartBeatCount) throws IOException, ClassNotFoundException{

        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), backupLFD2.backupServerPort);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject("heartBeat");

        System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (backupLFD2.num + 1) + " sending heartbeat to S" + (backupLFD.num + 1));
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " backupLFD " + (backupLFD2.num + 1) + " receives heartbeat from S" + (backupLFD.num + 1));
        }
    }

        public static void sendHeartBeatToGFD(boolean serverReachable) throws IOException, ClassNotFoundException{
            Socket socket = new Socket(GDFAddress, portGDF);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
    
            if (serverReachable) {
                String msg = String.format("LFD%d: add replica S%d", backupLFD2.num + 1, backupLFD2.num + 1);
                outputStream.writeObject(msg);
                System.out.println("[" + utilFunc.getTime() + "] " + msg);
            }
            else {
                String msg = String.format("LFD%d: delete replica S%d", backupLFD2.num + 1, backupLFD2.num + 1);
                //String msg = "LFD1: delete replica S1";
                outputStream.writeObject(msg);
                System.out.println("[" + utilFunc.getTime() + "] " + msg);
            }
        
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            String inputMessage = (String)inputStream.readObject();
    
            //Verify if the server successfully receives the heartbeat message
            if(inputMessage.equals("heartbeat message received")){
                System.out.println("[" + utilFunc.getTime() + "] " + " LFD" + (backupLFD2.num + 1) + "'s heartbeat received by GFD");
            }
        }
    }
