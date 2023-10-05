import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import util.utilFunc;

public class LFD1 {
    
    private static boolean stopTimer = false;
    private static int port = 9876;
    private static int portGDF = 9886;

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
                    //Increment heartBeatCount by 1
                    heartBeatCount++;

                } catch (ClassNotFoundException | IOException e) {
                    if (serverReachable) {
                        serverReachable = false;
                        try {
                            sendHeartBeatToGFD(serverReachable);
                        }
                        catch (ClassNotFoundException | IOException ee) {}
                    }
                }
            }
        }, 0, timeIntervalInt);
    }


    public static void sendHeartBeatMessage(int heartBeatCount) throws IOException, ClassNotFoundException{
        System.out.println("this LFD has port: " + port);
        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), port);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject("heartBeat");

        System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD1 sending heartbeat to S1");
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD1 receives heartbeat from S1");
        }
    }

    public static void sendHeartBeatToGFD(boolean serverReachable) throws IOException, ClassNotFoundException{
        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), portGDF);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        if (serverReachable) {
            String msg = "LFD1: add replica S1";
            outputStream.writeObject(msg);
            System.out.println("[" + utilFunc.getTime() + "] " + msg);
        }
        else {
            String msg = "LFD1: delete replica S1";
            outputStream.writeObject(msg);
            System.out.println("[" + utilFunc.getTime() + "] " + msg);
        }
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println("[" + utilFunc.getTime() + "] " + " LFD1's heartbeat received by GFD");
        }
    }
}