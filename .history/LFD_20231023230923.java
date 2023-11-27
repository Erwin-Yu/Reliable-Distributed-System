import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

import util.utilFunc;



public class LFD {
    
    private static int serverNumber = 0;
    static boolean stopTimer = false;
    public static int num = serverNumber; 
    private static int portGDF = 9886;
    private static int port = 9906; 
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

        //System.out.println("this is the server has port: " + (LFD.num + LFD.port));
        Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), LFD.port);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        outputStream.writeObject("heartBeat");

        System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (LFD.num + 1) + " sending heartbeat to S" + (LFD.num + 1));
    
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        String inputMessage = (String)inputStream.readObject();

        //Verify if the server successfully receives the heartbeat message
        if(inputMessage.equals("heartbeat message received")){
            System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (LFD.num + 1) + " receives heartbeat from S" + (LFD.num + 1));
        }
    }


        static String GDFAddress = "192.168.1.3";

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
                System.out.println("[" + utilFunc.getTime() + "] " + " LFD" + (LFD.num + 1) + "'s heartbeat received by GFD");
            }
        }
    }
