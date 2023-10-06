
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

public class server {
    private static ServerSocket newServer;
    private static String my_state;
    private int heartBeatCount = 0; 
    public static int num = 2; 
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

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        int port = 9876 + server.num;
        System.out.println("this is the server has port: " + port);
        newServer = new ServerSocket(port);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        server s = new server(newServer, "initial state");
        Socket newSocket = null;

        while(true){
            newSocket = s.newServer.accept();
            Runnable clientHandler = new ClientHandler(newSocket, s);
            executorService.execute(clientHandler);
        }
    }
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
                    // System.out.printf(); 
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

