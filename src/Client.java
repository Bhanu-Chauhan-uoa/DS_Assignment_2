import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String jsonData = reader.readLine();
            System.out.println("Received data from Aggregation Server: " + jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
