import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import data.messageTuple;

import java.time.Instant;

import util.utilFunc;

public class client{
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{

        int requestNum = 101;
        int clientId = 0;

        InetAddress[] hosts = new InetAddress[]{InetAddress.getByName("172.20.10.10"),
                                                InetAddress.getByName("172.20.10.8"),
                                                InetAddress.getByName("172.20.10.11")}; // InetAddress.getLocalHost();
        Socket[] sockets = new Socket[3];
        ObjectOutputStream[] outputStreams = new ObjectOutputStream[3]; 
        ObjectInputStream[] inputStreams = new ObjectInputStream[3]; 
        String[] inputMessages = new String[3];
        messageTuple[] serverMessageTuples = new messageTuple[3];
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Input a client Id:");
        String clientIdInStr = scanner.nextLine();
        Set<Integer> reqnum = new HashSet<Integer>();
        clientId = Integer.parseInt(clientIdInStr);

        while(true){
            for (int i = 0; i < 3; i++){
                try {
                    // sockets[i] = new Socket(host.getHostName(), 9876 + i);
                    sockets[i] = new Socket(hosts[i].getHostName(), 9876);
                    outputStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                } catch (Exception e){
                    continue; 
                } 
            }
            // System.out.print("Please Input a value for Changing Server State:");
            // String inputClientMessage = scanner.nextLine();

            String inputClientMessage = "Client " + clientId + " req " + requestNum;
            System.out.println("Client sending \"" + requestNum + "\"");
            

            for (int i = 0; i < 3; i++){
                try {
                    messageTuple sendTuple = new messageTuple(clientId, i + 1, requestNum, "request", inputClientMessage);
                    outputStreams[i].writeObject(sendTuple.toString());
                    System.out.println("[" + utilFunc.getTime() + "] Sent to Server " + (i + 1) + " " + sendTuple.toPrintString());
                } catch (Exception e) {
                    continue; 
                }
            }
            for (int i = 0; i < 3; i++){
                try {
                    inputStreams[i] = new ObjectInputStream(sockets[i].getInputStream());
                } catch (Exception e) {
                    continue; 
                }
            }
 
            for (int i = 0; i < 3; i++){
                try{
                    inputMessages[i] = (String)inputStreams[i].readObject();
                    serverMessageTuples[i] = messageTuple.fromString(inputMessages[i]);
                    System.out.println("[" + utilFunc.getTime() + "] Received from server " + (i + 1) + " " + serverMessageTuples[i].toPrintString());
                    if (!reqnum.add(serverMessageTuples[i].getRequestNum())){
                        System.out.println("request_num " + serverMessageTuples[i].getRequestNum() + " : Discarded duplicate reply from " + "Server " + (i + 1));
                    }
                } catch (Exception e){
                    continue; 
                }
            }
            requestNum++;
            System.out.println();

            Thread.sleep(5000);
        }

    }
}


