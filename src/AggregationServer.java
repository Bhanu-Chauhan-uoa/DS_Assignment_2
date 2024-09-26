import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("AggregationServer is running and waiting for connections...");

            while (true) {
                // Accept connection from ContentServer
                Socket contentServerSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(contentServerSocket.getInputStream()));
                
                // Receive the JSON data
                String receivedData = reader.readLine();
                System.out.println("Received JSON data from ContentServer: " + receivedData);

                // Now, wait for client connections to send the data
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                
                // Send the received JSON data to the client
                writer.println(receivedData);
                System.out.println("Sent JSON data to Client: " + receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
