import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utils {
    public static Object[] serverAndPort(String arguments) {
        String serverName;
        int portNumber;
    
        // Split by colon to extract server name and port
        String[] parts = arguments.split(":");
    
        switch (parts.length) {
            case 2: // Format: "servername:port"
                serverName = parts[0];
                portNumber = Integer.parseInt(parts[1]);
                break;
    
            case 3: // Format: "http://servername:port" or "http://servername.domain:port"
                if (parts[0].startsWith("http")) {
                    serverName = parts[1].substring(2); // Skip "//" in URL
                    portNumber = Integer.parseInt(parts[2]);
                } else {
                    throw new IllegalArgumentException("Invalid server URL format.");
                }
                break;
    
            default:
                throw new IllegalArgumentException("Invalid server URL format. Use 'servername:port' or 'http://servername:port'.");
        }
    
        return new Object[]{serverName, portNumber};
    }

    public static String[] handleCSFileInputs(String[] arguments) {
        return Arrays.copyOfRange(arguments, 2, arguments.length);
    }    

    public static void updateContentServerBackUpFile(ArrayNode data, String contentServerID){
        ObjectMapper map = new ObjectMapper();
        try (FileWriter fw = new FileWriter("CS_Backup/backup" + contentServerID +".json");
            BufferedWriter bfw = new BufferedWriter(fw)) {
            ObjectNode root = map.createObjectNode();
            root.set("data", data);
            String JSONOutput = map.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            bfw.write(JSONOutput);
        } catch (IOException e) {
            System.err.println("Something went wrong while updating Content Server Backup.\nTry again later.");
            System.exit(1);
        }
    }

    public static void updateAggregationServerBackUpFile(ArrayNode data, int lamport, ArrayNode allCsID, ObjectNode allProcessId, ArrayNode processIdCounter){
        ObjectMapper map = new ObjectMapper();
        try (FileWriter fw = new FileWriter("backup_data/backup.json");
             BufferedWriter bfw = new BufferedWriter(fw)) {
            ObjectNode root = map.createObjectNode();
            root.put("Lamport", lamport);
            root.put("All_CS_ID", allCsID);
            root.put("All_Process_ID", allProcessId);
            root.put("Process_ID", processIdCounter);
            root.put("data", data);
            String JSONOutput = map.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            bfw.write(JSONOutput);
        } catch (IOException e) {
            System.err.println("Please Try again later!\nSomething went wrong while updating Aggregation Server Backup.");
            System.exit(1);
        }
    }
    public static Object[] handleHeaderAndBody(BufferedReader reader) throws IOException {
        String line;
        int contentLength = 0;
        StringBuilder reqBuilder = new StringBuilder();
        Map<String, String> headersMap = new HashMap<>();
        while (!(line = reader.readLine()).isEmpty()) {
            reqBuilder.append(line).append("\n");
            if (line.contains(": ")) {
                String[] parts = line.split(": ", 2);
                headersMap.put(parts[0], parts[1]);
                if (parts[0].equals("Content-Length")) {
                    contentLength = Integer.parseInt(parts[1].trim());
                }
            }else{
                headersMap.put("method", line);
            }
        }

        ObjectMapper map = new ObjectMapper();
        ArrayNode deserializedArr = map.createArrayNode();
        if (contentLength > 0) {
            char[] body = new char[contentLength];
            reader.read(body, 0, contentLength);


            JsonNode node = map.readTree(new String(body));
            if (node.isArray()) {
                deserializedArr = (ArrayNode) node;
            }
        }
        Object[] reqAndBody = {headersMap, deserializedArr};
        return reqAndBody;
    }


    public static ArrayNode compareDuplicatesIfAny(ArrayNode body, String CS_ID, int Aggregation_current_lamport, ObjectNode allProcessId) {
        Map<String, JsonNode> uniqueEntries = new HashMap<>();
    
        for (int i = 0; i < body.size(); i++) {
            JsonNode node = body.get(i);
    
            if (node instanceof ObjectNode) {
                ObjectNode objNode = (ObjectNode) node;
                objNode.put("Aggregation_current_lamport", Aggregation_current_lamport);
                objNode.put("CS_ID", CS_ID);
                objNode.put("processId", allProcessId.get(CS_ID));
            }
    
            String id = node.get("id").asText();
            String dateTime = node.get("local_date_time_full").asText();
    
            if (uniqueEntries.containsKey(id)) {
                String existingLocalDateTime = uniqueEntries.get(id).get("local_date_time_full").asText();
                if (dateTime.compareTo(existingLocalDateTime) > 0) {
                    uniqueEntries.put(id, node);
                }
            } else {
                uniqueEntries.put(id, node);
            }
        }
    
        ArrayNode mainArr = new ObjectMapper().createArrayNode();
        Iterator<JsonNode> iterator = uniqueEntries.values().iterator();
        while (iterator.hasNext()) {
            JsonNode value = iterator.next();
            mainArr.add(value);
        }
        return mainArr;
    }    
}
