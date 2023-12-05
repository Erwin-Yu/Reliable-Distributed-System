import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import data.checkPointMessageTuple;
import data.messageTuple;
import util.utilFunc;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class activeServer {

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

    static ServerSocket newServer;
    private static String my_state;
    private int heartBeatCount = 0; 

    // Variables that are needed to change:
    //Checkpoint with backup Servers
    public static int num = 1;
    public static int i_am_ready = 1;
    public static int activeBackupServerNum = 2;
    public static int checkpointFreq = 500;
    public static int checkpointCount = 0;
    public Timer timer;
    public Timer resetTimer;
    public AtomicInteger count;
    public List<String> high_watermark_request_num;
    // Variables for connections with backup servers
    //Go to line 99 and manually change the host addresses  and ports of backup servers

    void addheartBeat(){
        this.heartBeatCount = this.heartBeatCount + 1; 
    }

    int getheartBeat(){
        return this.heartBeatCount; 
    }


    public activeServer(ServerSocket newServer, String my_state) {
        this.newServer = newServer; 
        this.my_state = my_state;
        this.timer = new Timer();
        this.high_watermark_request_num = new ArrayList<>();
        // Print initial state of the server
        System.out.println("Initial state of primary server: " + this.my_state);
    }

    synchronized void changeCheckPointCount(int count) {
        this.checkpointCount = count;
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
        // Check if the file exists
        Path filePath = Paths.get("ready.txt");
        if (Files.notExists(filePath)) {

            try {
                // Create the file if it doesn't exist
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception or log it appropriately
                return; // Exit the program or handle the situation accordingly
            }
        } else {
            i_am_ready = 0;
        }

        int port = 9876;
        System.out.println("this is the server has port: " + port);
        newServer = new ServerSocket(port);
        
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        activeServer s = new activeServer(newServer, "initial state");
        Socket newSocket = null;

        // schedule the normal checkpoint 
        s.timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                try {
                    sendCheckPointMessageToBackUps(true, s);
                } catch (ClassNotFoundException | IOException e) {
           
                }
            }
        }, 0, checkpointFreq);

        while(true){
            newSocket = s.newServer.accept();
            Runnable clientHandler = new ClientHandler(newSocket, s);
            executorService.execute(clientHandler);
            // count.incrementAndGet();
        }
    }


    public static void sendCheckPointMessageToBackUps(boolean serverReachable, activeServer s) throws IOException, ClassNotFoundException{
        
        InetAddress[] backUpServerhosts = new InetAddress[]{
                                            InetAddress.getByName("172.26.13.22"),
                                            InetAddress.getByName("")
                                        }; 

        int[] backUpServerPorts = new int[]{
            9876,
            9876
            };

        Socket[] sockets = new Socket[activeBackupServerNum];
        ObjectOutputStream[] outputStreams = new ObjectOutputStream[activeBackupServerNum]; 
        try {
 
            for (int i = 0; i < activeBackupServerNum; i++){

                 try {
                    sockets[i] = new Socket();
                    sockets[i].connect(new InetSocketAddress(backUpServerhosts[i], backUpServerPorts[i]), 500); // Set a timeout of 1000 milliseconds (1 second)
                    outputStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                } catch (IOException e) {
                    // Server is unreachable or timeout occurred, skip it
                    if (sockets[i] != null) {
                        sockets[i].close(); // Close the socket if it was opened
                    }
                    continue;
                }
            }

            checkPointMessageTuple checkPointMessage = new checkPointMessageTuple(s.my_state, s.checkpointCount, s.num);
            for (int i = 0; i < activeBackupServerNum; i++){
                try {    
                    if(outputStreams[i] != null){
                        outputStreams[i].writeObject(checkPointMessage.toString());
                        System.out.println(ANSI_GREEN + "[" + utilFunc.getTime() + "] Sent to Backup Server " + (i + 1) + " " + checkPointMessage.toString() + ANSI_RESET);
                    }

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
}


class ClientHandler implements Runnable {
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

    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private activeServer server;

    public ClientHandler(Socket socket, activeServer s) throws IOException {
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
                System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num % 3 + 1) + "receives heartbeat from LFD" + (server.num % 3 + 1) + ANSI_RESET);
                outputStream.writeObject("heartbeat message received");
                System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num % 3 + 1) + " sents heartbeat to LFD" + (server.num % 3 + 1) + ANSI_RESET);
                this.server.addheartBeat();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(clientMessage.startsWith("<checkpoint,") && clientMessage.endsWith(">"))  {
            if (this.server.i_am_ready == 1){
                return; 
            } else {
                this.server.i_am_ready = 1;
            }
            checkPointMessageTuple checkPointMessage = checkPointMessageTuple.fromString(clientMessage);  
            
            System.out.println(ANSI_GREEN + "[" + utilFunc.getTime() + "] " + " Received CheckPoint message: " + checkPointMessage.toString() + ANSI_RESET);

            //Print my_state beore and after the procession of the check point message
            this.server.changeState(checkPointMessage.getNewStateValue());
            this.server.changeCheckPointCount(checkPointMessage.getCheckpointCount());
            System.out.println(ANSI_GREEN + "[" + utilFunc.getTime() + "] " + " my_state_S" + (server.num % 3 + 1) + " =" + this.server.getState() + " after processing the CheckPoint message: " + checkPointMessage.toString() + ANSI_RESET);
        
        }else{
            //Otherwise the message is from one of the clients and we convert it into 'messageTuple' object
            messageTuple clientMessageTuple = messageTuple.fromString(clientMessage);
            if (this.server.i_am_ready == 0) {
                this.server.high_watermark_request_num.add(clientMessageTuple.newStateValue);
                return;
            }
            
            System.out.println(utilFunc.getTime() + " Received " + clientMessageTuple.toPrintString());
        
            //Print my_state beore and after the procession of the message
            System.out.println(utilFunc.getTime() + " my_state_S" + (server.num % 3 + 1) + " =" + this.server.getState() + " before processing " + clientMessageTuple.toPrintString());
            this.server.changeState(clientMessageTuple.getNewStateValue());
            System.out.println(utilFunc.getTime() + " my_state_S" + (server.num % 3 + 1) + " =" + this.server.getState() + " after processing " + clientMessageTuple.toPrintString());

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
