import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
 
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int SOCKET_READ_TIMEOUT = 5000; // 5 seconds timeout

    // 192.168.1.3
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        
        
        int activeServerNum = 3; // CHANGE ME!!!
        int requestNum = 101;
        int clientId = 0;

        // CHANGE ME!!!
        InetAddress[] hosts = new InetAddress[]{InetAddress.getByName("172.26.26.131"),
                                                InetAddress.getByName("172.26.29.61"),
                                                InetAddress.getByName("172.26.95.8")};
        
        // InetAddress[] hosts = new InetAddress[]{InetAddress.getByName("192.168.1.3") }; // InetAddress.getLocalHost();
        
        
        Socket[] sockets = new Socket[activeServerNum];
        ObjectOutputStream[] outputStreams = new ObjectOutputStream[activeServerNum]; 
        ObjectInputStream[] inputStreams = new ObjectInputStream[activeServerNum]; 
        
        
        String[] inputMessages = new String[activeServerNum];
        messageTuple[] serverMessageTuples = new messageTuple[activeServerNum];
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Input a client Id:");
        String clientIdInStr = scanner.nextLine();
        Set<Integer> reqnum = new HashSet<Integer>();
        clientId = Integer.parseInt(clientIdInStr);


        
        while(true){

            for (int i = 0; i < activeServerNum; i++){
                try {
                    sockets[i] = new Socket();
                    sockets[i].connect(new InetSocketAddress(hosts[i], 9876), 500);
                    outputStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                } catch (Exception e){
                    continue; 
                } 
            }
            // System.out.print("Please Input a value for Changing Server State:");
            // String inputClientMessage = scanner.nextLine();

            String inputClientMessage = "Client " + clientId + " req " + requestNum;
            System.out.println("Client sending \"" + requestNum + "\"");
            

            for (int i = 0; i < activeServerNum; i++){
                try {
                    if (outputStreams[i] != null) {
                        messageTuple sendTuple = new messageTuple(clientId, i + 1, requestNum, "request", inputClientMessage);
                        outputStreams[i].writeObject(sendTuple.toString());
                        String color = "";
                        switch (i) {
                            case 0:
                                color = ANSI_CYAN;
                                break;
                            case 1:
                                color = ANSI_PURPLE;
                                break;
                            default:
                                color = ANSI_YELLOW;
                                break;
                        }
                        System.out.println(color + "[" + utilFunc.getTime() + "] Sent to Server " + (i + 1) + " " + sendTuple.toPrintString() + ANSI_RESET);
                    }
                } catch (Exception e) {
                    continue; 
                }
            }
            for (int i = 0; i < activeServerNum; i++){
                try {
                    inputStreams[i] = new ObjectInputStream(sockets[i].getInputStream());
                } catch (Exception e) {
                    continue; 
                }
            }
 
            for (int i = 0; i < activeServerNum; i++){
                try{
                    if (inputStreams[i] != null) {
                        sockets[i].setSoTimeout(SOCKET_READ_TIMEOUT);
                        inputMessages[i] = (String)inputStreams[i].readObject();
                        serverMessageTuples[i] = messageTuple.fromString(inputMessages[i]);
                        String color = "";
                        switch (i) {
                            case 0:
                                color = ANSI_CYAN;
                                break;
                            case 1:
                                color = ANSI_PURPLE;
                                break;
                            default:
                                color = ANSI_YELLOW;
                                break;
                        }
                        System.out.println(color + "[" + utilFunc.getTime() + "] Received from server " + (i + 1) + " " + serverMessageTuples[i].toPrintString() + ANSI_RESET);
                        if (!reqnum.add(serverMessageTuples[i].getRequestNum())){
                            System.out.println(ANSI_RED + "request_num " + serverMessageTuples[i].getRequestNum() + " : Discarded duplicate reply from " + "Server " + (i + 1) + ANSI_RESET);
                        }
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


