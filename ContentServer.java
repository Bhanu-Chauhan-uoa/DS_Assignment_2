import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ContentServer {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000)) {
            String jsonData = "{\"key\": \"value\"}";
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}