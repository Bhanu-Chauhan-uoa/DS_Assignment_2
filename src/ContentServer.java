import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class ContentServer {
    public static void main(String[] args) {
        String[] weatherTxtFiles = {"data1.txt", "data2.txt", "data3.txt"};
        ArrayNode arr = CreateJSON.readWeatherData(weatherTxtFiles);
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