import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.checkPointMessageTuple;
import data.messageTuple;
import util.utilFunc;

import java.util.concurrent.atomic.AtomicInteger;


public class server {
    static ServerSocket newServer;
    private static String my_state;
    private int heartBeatCount = 0; 

    // Variables that are needed to change:
        //Checkpoint with backup Servers
        public static int PrimaryServerId =0;
        public static int num = PrimaryServerId;
        public static int activeBackupServerNum = 2;
        public static int checkpointFreq = 2000;
        public static int checkpointCount = 0;

        // Variables for connections with backup servers

        //Go to line 99 and manually change the host addresses  and ports of backup servers
    



    void addheartBeat(){
        this.heartBeatCount = this.heartBeatCount + 1; 
    }

    int getheartBeat(){
        return this.heartBeatCount; 
    }
    public server(ServerSocket newServer, String my_state) {
        this.newServer = newServer; 
        this.my_state = my_state;
    }
    
    synchronized void changeState(String newValue) {
        this.my_state = newValue;
    }
    synchronized String getState() {
        return my_state;
    }

    public synchronized void incrementCheckPointCount() {
        this.checkpointCount++;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        Timer timer;
        int port = 9916;
        System.out.println("this is the server has port: " + port);

        newServer = new ServerSocket(port);
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        server s = new server(newServer, "initial state");
        Socket newSocket = null;

        // int clientHandlerCount = 0;
        AtomicInteger count = new AtomicInteger(0);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                try {
                    if(count.get() > 0 ){
                        sendCheckPointMessageToBackUps(true, s);
                    }
                } catch (ClassNotFoundException | IOException e) {
           
                }
            }
        }, 0, checkpointFreq);

        while(true){
            newSocket = s.newServer.accept();
            Runnable clientHandler = new ClientHandler(newSocket, s);
            executorService.execute(clientHandler);
            count.incrementAndGet();
            

        }
    }


    public static void sendCheckPointMessageToBackUps(boolean serverReachable,server s) throws IOException, ClassNotFoundException{
      
        
        InetAddress[] backUpServerhosts = new InetAddress[]{
                                            InetAddress.getByName("192.168.1.3"),
                                            InetAddress.getByName("192.168.1.3")
                                        }; 

        int[] backUpServerPorts = new int[]{
            9916, 
            9946
            };

        Socket[] sockets = new Socket[activeBackupServerNum];
        ObjectOutputStream[] outputStreams = new ObjectOutputStream[activeBackupServerNum]; 
        try {
 
            for (int i = 0; i < activeBackupServerNum; i++){
                try {
                    // sockets[i] = new Socket(host.getHostName(), 9876 + i);
                    sockets[i] = new Socket(backUpServerhosts[i].getHostName(), backUpServerPorts[i]);
                    outputStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                } catch (Exception e){
                    continue; 
                } 
            }
            checkPointMessageTuple checkPointMessage = new checkPointMessageTuple(s.my_state, s.checkpointCount);
            for (int i = 0; i < activeBackupServerNum; i++){
                try {    
                    outputStreams[i].writeObject(checkPointMessage.toString());
                    System.out.println("[" + utilFunc.getTime() + "] Sent to Backup Server " + (i + 1) + " " + checkPointMessage.toString());


                } catch (Exception e){
                    continue; 
                } 
                
            }

            if(serverReachable){
                //Increment CheckPoint Count value
                s.incrementCheckPointCount();
            }

        }catch (Exception e) {
        }
    }

        // if (serverReachable) {
        //     String msg = String.format("LFD%d: add replica S%d", LFD.num + 1, LFD.num + 1);
        //     outputStream.writeObject(msg);
        //     System.out.println("[" + utilFunc.getTime() + "] " + msg);
        // }
        // else {
        //     String msg = String.format("LFD%d: delete replica S%d", LFD.num + 1, LFD.num + 1);
        //     //String msg = "LFD1: delete replica S1";
        //     outputStream.writeObject(msg);
        //     System.out.println("[" + utilFunc.getTime() + "] " + msg);
        // }

        // ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        // String inputMessage = (String)inputStream.readObject();

        // //Verify if the server successfully receives the heartbeat message
        // if(inputMessage.equals("heartbeat message received")){
        //     System.out.println("[" + utilFunc.getTime() + "] " + " LFD" + (LFD.num + 1) + "'s heartbeat received by GFD");
        // }




}


class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private server server;
    public ClientHandler(Socket socket, server s) throws IOException {
        this.clientSocket = socket;
        this.server = s; 
        this.inputStream = new ObjectInputStream(socket.getInputStream()); 
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        String clientMessage;
        try {
            clientMessage = (String) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (clientMessage.equals("heartBeat")){
                try {
           
                    System.out.println("[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num + 1) + "receives heartbeat from LFD" + (server.num + 1));
                    outputStream.writeObject("heartbeat message received");
                    System.out.println("[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num + 1) + " sents heartbeat to LFD" + (server.num + 1));
                    
                    this.server.addheartBeat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }else{
            //Otherwise the message is from one of the clients and we convert it into 'messageTuple' object
            messageTuple clientMessageTuple = messageTuple.fromString(clientMessage);
            System.out.println(utilFunc.getTime() + " Received " + clientMessageTuple.toPrintString());
        
            //Print my_state beore and after the procession of the message
            System.out.println(utilFunc.getTime() + " my_state_S" + (server.num + 1) + " =" + this.server.getState() + " before processing " + clientMessageTuple.toPrintString());
            this.server.changeState(clientMessageTuple.getNewStateValue());
            System.out.println(utilFunc.getTime() + " my_state_S" + (server.num + 1) + " =" + this.server.getState() + " after processing " + clientMessageTuple.toPrintString());

            messageTuple replyMessageTuple = clientMessageTuple;
            replyMessageTuple.setMessageDirection("reply");
            
            try {
                outputStream.writeObject(replyMessageTuple.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.out.println(utilFunc.getTime() + " Sending " + clientMessageTuple.toPrintString());
        }
    }

}