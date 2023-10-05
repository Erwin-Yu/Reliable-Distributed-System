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

import data.messageTuple;

import java.time.Instant;

import util.utilFunc;

public class client{
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{

        int requestNum = 101;
        int clientId = 0;

        InetAddress host = InetAddress.getLocalHost();
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
                    sockets[i] = new Socket(host.getHostName(), 9876 + i);
                    outputStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                } catch (Exception e){
                    continue; 
                } 
            }
            System.out.print("Please Input a value for Changing Server State:");
            String inputClientMessage = scanner.nextLine();
            messageTuple sendTuple = new messageTuple(clientId, 1, requestNum, "request", inputClientMessage);

            for (int i = 0; i < 3; i++){
                try {
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
                        System.out.println("request_num " + serverMessageTuples[i].getRequestNum() + " : Discarded duplicate reply from " + "Server " + (i + i));
                    }
                } catch (Exception e){
                    continue; 
                }
            }
            requestNum++;
        }

    }
}


