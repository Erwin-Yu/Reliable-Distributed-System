
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

import data.messageTuple;
import util.utilFunc;

public class server {

    private static ServerSocket newServer;
    private static int port = 9876;

    public static void main(String[] args) throws IOException, ClassNotFoundException{

        newServer = new ServerSocket(port);
        Socket newSocket = null;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        
        int counter = 0;

        String my_state_S1 = "initial state value";

        while(true){

            // System.out.println("Waiting for new Message from Client");

            newSocket = newServer.accept();
            inputStream = new ObjectInputStream(newSocket.getInputStream());
            String clientMessage = (String) inputStream.readObject();
            outputStream = new ObjectOutputStream(newSocket.getOutputStream());
    
            //Verify if the message is heartbeat message from LFD
            if (clientMessage.equals("heartBeat")){
                outputStream.writeObject("heartbeat message received");
            
            }else{
                //Otherwise the message is from one of the clients and we convert it into 'messageTuple' object
                messageTuple clientMessageTuple = messageTuple.fromString(clientMessage);
                System.out.println(utilFunc.getTime() + " Received " + clientMessageTuple.toPrintString());
            
                //Print my_state beore and after the procession of the message
                System.out.println(utilFunc.getTime() + " my_state_S1 =" + my_state_S1 + " before processing " +clientMessageTuple.toPrintString());
                my_state_S1 = clientMessageTuple.getNewStateValue();
                System.out.println(utilFunc.getTime() + " my_state_S1 =" + my_state_S1 + " after processing " +clientMessageTuple.toPrintString());

                messageTuple replyMessageTuple = clientMessageTuple;
                replyMessageTuple.setMessageDirection("reply");
                outputStream.writeObject(replyMessageTuple.toString());
                System.out.println(utilFunc.getTime() + " Sending " + clientMessageTuple.toPrintString());
            }
        }
        // newServer.close();
        // inputStream.close();
        // outputStream.close();
        // newSocket.close();
    }
}
