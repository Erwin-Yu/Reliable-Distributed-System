import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import data.messageTuple;
import util.utilFunc;

public class client{

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    public static void main(String[] args) throws IOException, ClassNotFoundException{

        int requestNum = 100;
        int clientId = 0;

        InetAddress host = InetAddress.getLocalHost();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please Input a client Id:");
        String clientIdInStr = scanner.nextLine();
        clientId = Integer.parseInt(clientIdInStr);
        scanner.close(

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new RequestTask(clientId, requestNum, host), 0, 2000);
    }

    public static String generateRandomText(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private static class RequestTask extends TimerTask {

        public int requestNum;
        public int clientId;
        public Socket socket;
        public ObjectOutputStream outputStream;
        public ObjectInputStream inputStream;
        public InetAddress host;


        public RequestTask(int clientId, int requestNum, InetAddress host) {
            this.clientId = clientId;
            this.requestNum = requestNum;
            this.host = host;
        }

        @Override
        public void run()  {
            try {
                socket = new Socket(host.getHostName(), 9876);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                // Create a messageTuple and send it to the client
                String inputClientMessage = "" + requestNum;
                messageTuple sendTuple = new messageTuple(clientId, 1, requestNum, "request", inputClientMessage);

                outputStream.writeObject(sendTuple.toString());

                System.out.println("[" + utilFunc.getTime() + "] Sent "+ sendTuple.toPrintString());
                inputStream = new ObjectInputStream(socket.getInputStream());
                String inputMessage = (String)inputStream.readObject();
                messageTuple serverMessageTuple = messageTuple.fromString(inputMessage);

                System.out.println("[" + utilFunc.getTime() + "] Received " + serverMessageTuple.toPrintString());
                requestNum++;
            } catch (ClassNotFoundException | IOException e) {
                // e.printStackTrace();
                System.out.println("Server is dead!!!!");
                return;
            }
        }
    }
    
}


