import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class RM {
    private static int memberCount = 0;
    private static List<String> membership = new ArrayList<>();
    int port= 9896;
    // Create threads for each server socket
    Thread thread = createSocketThread(port);
    thread.start();

    System.out.println("GFD: 0 members");
}
