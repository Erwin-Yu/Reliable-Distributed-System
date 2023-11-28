
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
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import data.checkPointMessageTuple;

import data.messageTuple;

import util.utilFunc;

public class backupServer2 {
    private static ServerSocket newServer;
    private static String my_state;
    private int heartBeatCount = 0; 

    // Variables that are needed to change:

    // *** every backupServer must identify their unique Ids (server 2 and 3 are backup servers)
    public static int backUpServerId =2;
    public static int num = backUpServerId;
    public static int i_am_ready = 0;
    public static int currentServerPort = 9946;
    public static int checkpointCount = 0;

    void addheartBeat(){
        this.heartBeatCount = this.heartBeatCount + 1; 
    }

    int getheartBeat(){
        return this.heartBeatCount; 
    }
    public backupServer2(ServerSocket newServer, String my_state) {
        this.newServer = newServer; 
        this.my_state = my_state;
        this.checkpointCount = 0;
    }
    synchronized void changeState(String newValue) {
        this.my_state = newValue;
    }

    synchronized void changeCheckPointCount(int count) {
        this.checkpointCount = count;
    }

    synchronized String getState() {
        return my_state;
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException{

        int port = currentServerPort;

        System.out.println("this is the server has port: " + port);
        newServer = new ServerSocket(port);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        backupServer2 s = new backupServer2(newServer, "initial state");
        Socket newSocket = null;

        while(true){
            newSocket = s.newServer.accept();
            Runnable clientHandler = new ClientHandler(newSocket, s);
            executorService.execute(clientHandler);
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
    private backupServer2 server;

    public ClientHandler(Socket socket, backupServer2 s) throws IOException {
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
                    // System.out.printf(); 
                    System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num + 1) + "receives heartbeat from LFD" + (server.num + 1) + ANSI_RESET);
                    outputStream.writeObject("heartbeat message received");
                    System.out.println(ANSI_RED + "[" + utilFunc.getTime() + "] " + this.server.getheartBeat() + " Server " + (server.num + 1) + " sents heartbeat to LFD" + (server.num + 1) + ANSI_RESET);
                    this.server.addheartBeat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
       
                // case when the backup server receives the checkpoint message from the Primary Server replica
            }else if(clientMessage.startsWith("<checkpoint,") && clientMessage.endsWith(">")){
           
            checkPointMessageTuple checkPointMessage = checkPointMessageTuple.fromString(clientMessage);           
            System.out.println(ANSI_GREEN + "[" + utilFunc.getTime() + "] " + " Received CheckPoint message: " + checkPointMessage.toString() + ANSI_RESET);

            //Print my_state beore and after the procession of the check point message
            this.server.changeState(checkPointMessage.getNewStateValue());
            this.server.changeCheckPointCount(checkPointMessage.getCheckpointCount());
            System.out.println(ANSI_GREEN + "[" + utilFunc.getTime() + "] " + " my_state_S" + (server.num + 1) + " =" + this.server.getState() + " after processing the CheckPoint message: " + checkPointMessage.toString() + ANSI_RESET);
        
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
