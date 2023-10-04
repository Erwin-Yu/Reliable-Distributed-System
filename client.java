import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import data.messageTuple;

import java.time.Instant;

import util.utilFunc;

public class client{
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{

        int requestNum = 101;
        int clientId = 0;

        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Input a client Id:");
        String clientIdInStr = scanner.nextLine();
        clientId = Integer.parseInt(clientIdInStr);

        while(true){

            socket = new Socket(host.getHostName(), 9876);
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            System.out.print("Please Input a value for Changing Server State:");
            String inputClientMessage = scanner.nextLine();

            messageTuple sendTuple = new messageTuple(clientId, 1, requestNum, "request", inputClientMessage);

            outputStream.writeObject(sendTuple.toString());

            System.out.println("[" + utilFunc.getTime() + "] Sent "+ sendTuple.toPrintString());
            inputStream = new ObjectInputStream(socket.getInputStream());
            String inputMessage = (String)inputStream.readObject();
            messageTuple serverMessageTuple = messageTuple.fromString(inputMessage);

            System.out.println("[" + utilFunc.getTime() + "] Received " + serverMessageTuple.toPrintString());
            requestNum++;
        }

    }
}


