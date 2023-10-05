import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

import util.utilFunc;



public class LFD {
    
    static boolean stopTimer = false;
    Integer[] ports = {9876, 9877, 9878};
    int globalIndex = 0; 
      
    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int heartBeatFreq = 1;
        int port = 9876;
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
            @Override
            public void run(){

                try {
                    sendHeartBeatMessage(heartBeatCount);
                    //Increment heartBeatCount by 1
                    heartBeatCount++;

                } catch (ClassNotFoundException | IOException e) {

                    System.out.println("HeartBeat message failed... / Server is currently unreachable...");
                    //Terminate the heartbeat service
                    // timer.cancel();
                }
            }
        }, 0, timeIntervalInt);


    }


    public static void sendHeartBeatMessage(int heartBeatCount) throws IOException, ClassNotFoundException{

            int port = 9876;
            int num = 2; 
            System.out.println("this is the server has port: " + (num + port));
            Socket socket = new Socket(InetAddress.getLocalHost().getHostName(), port + num);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeObject("heartBeat");

            System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (num + 1) + " sending heartbeat to S" + (num + 1));
        
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            String inputMessage = (String)inputStream.readObject();

            //Verify if the server successfully receives the heartbeat message
            if(inputMessage.equals("heartbeat message received")){
                System.out.println("[" + utilFunc.getTime() + "] " + heartBeatCount + " LFD " + (num + 1) + " receives heartbeat from S" + (num + 1));
            }
        }
    }
