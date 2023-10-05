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

import data.messageTuple;
import util.utilFunc;

public class server2 {
    private static ServerSocket newServer;
    private static String my_state;
    private static int port = 9877;
    private int heartBeatCount = 0; 
    void addheartBeat(){
        this.heartBeatCount = this.heartBeatCount + 1; 
    }
    int getheartBeat(){
        return this.heartBeatCount; 
    }
    public server2(ServerSocket newServer, String my_state) {
        this.newServer = newServer; 
        this.my_state = my_state;
    }
    synchronized void changeState(String newValue) {
        this.my_state = newValue;
    }
    synchronized String getState() {
        return my_state;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        boolean newServerSucc = false;
        while (!newServerSucc) {
            try {
                newServer = new ServerSocket(port);
                newServerSucc = true;
            } catch (Exception e) {
                port++;
                continue; 
            }
        }
        System.out.println("this is the server2 has port: " + port);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        server2 s = new server2(newServer, "initial state");
        Socket newSocket = null;

        while(true){
            newSocket = s.newServer.accept();
            Runnable clientHandler = new ClientHandler2(newSocket, s);
            executorService.execute(clientHandler);
        }
    }
}


class ClientHandler2 implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private server2 server2;
    public ClientHandler2(Socket socket, server2 s) throws IOException {
        this.clientSocket = socket;
        this.server2 = s; 
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
                    System.out.println("[" + utilFunc.getTime() + "] " + this.server2.getheartBeat() + " Server receives heartbeat from LFD");
                    outputStream.writeObject("heartbeat message received");
                    System.out.println("[" + utilFunc.getTime() + "] " + this.server2.getheartBeat() + " Server sents heartbeat to LFD");
                    this.server2.addheartBeat();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                //Otherwise the message is from one of the clients and we convert it into 'messageTuple' object
                messageTuple clientMessageTuple = messageTuple.fromString(clientMessage);
                System.out.println(utilFunc.getTime() + " Received " + clientMessageTuple.toPrintString());
            
                //Print my_state beore and after the procession of the message
                System.out.println(utilFunc.getTime() + " my_state_S2 =" + this.server2.getState() + " before processing " + clientMessageTuple.toPrintString());
                this.server2.changeState(clientMessageTuple.getNewStateValue());
                System.out.println(utilFunc.getTime() + " my_state_S2 =" + this.server2.getState() + " after processing " + clientMessageTuple.toPrintString());

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