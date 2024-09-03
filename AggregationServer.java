import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    private static String receivedData;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                // Accept connection from Content Server
                Socket contentServerSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(contentServerSocket.getInputStream()));
                receivedData = reader.readLine();
                System.out.println("Received data from Content Server: " + receivedData);

                // Now, wait for client connections
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println(receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
