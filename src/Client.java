import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class Client {
    private static LamportClock clock = new LamportClock();
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Error: Missing server URL argument.");
                System.exit(1);
            }
            
            // Assign stationID if provided; otherwise, default to an empty string
            String stationID = (args.length > 1) ? args[1] : "";            
            Object[] serverAndPort = Utils.serverAndPort(args[0]);
            String serverName = (String) serverAndPort[0];
            int portNumber = (int) serverAndPort[1];
            Socket socket = new Socket(serverName, portNumber);
            OutputStream opStream = socket.getOutputStream();
            String lamportClockHeader = "Lamport-Clock: " + clock.getCurrTime() + "\r\n";
            String HostContent = "Host: " + serverName + "\r\n\r\n";
            String req = "GET /?stationId=" + stationID + " HTTP/1.1\r\n" +
                    lamportClockHeader +
                    HostContent;

            opStream.write(req.getBytes());
            opStream.flush();
            InputStream ipStream = socket.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(ipStream));
            Object[] headerAndBody = Utils.handleHeaderAndBody(rd);
            Map<String, String> headersMap = (Map<String, String>) headerAndBody[0];
            ArrayNode body = (ArrayNode) headerAndBody[1];

            System.out.println(headersMap.get("method"));
            System.out.println("\n");

            for (int i = 0; i < body.size(); i++) {
                JsonNode arrayNode = body.get(i);
                
                // Use the Iterator to traverse the elements
                Iterator<JsonNode> elements = arrayNode.elements();
                while (elements.hasNext()) {
                    JsonNode objNode = elements.next();
                    
                    // Use the traditional for loop to go through the fields
                    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = objNode.fields();
                    while (fieldsIterator.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fieldsIterator.next();
                        JsonNode val = entry.getValue();
                        String k = entry.getKey();
                        
                        if ("CS_ID".equals(k) || "Aggregation_current_lamport".equals(k) || "processId".equals(k)) {
                            continue; // Use continue instead of return to skip the current entry
                        }
                        System.out.println(k + " --> " + val);
                    }
                    System.out.println("\n\n");
                }
            }            
        }
        catch(Exception e) {
            System.err.println("Aggregation Server is not ON. Please check the Aggregation server first.\nThen try to run client server.");
        }
    }
}