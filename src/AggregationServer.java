import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AggregationServer {
    private static ArrayNode data = new ObjectMapper().createArrayNode();
    private static ObjectNode PId = new ObjectMapper().createObjectNode();
    private static ArrayNode PIdCounter = new ObjectMapper().createArrayNode().add(0);
    private static LamportClock lampClock = new LamportClock();
    private static ArrayNode CSId = new ObjectMapper().createArrayNode();
    private static final Map<String, Timer> timers = new HashMap<>();

    public static void activateTimer(String id) {
        Timer timer = new Timer();
        timers.put(id, timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (CSId) {
                    int i=0;
                    while(i < CSId.size()){
                        JsonNode node = CSId.get(i);
                        if (node.isTextual() && node.asText().equals(id)) {
                            CSId.remove(i);
                            break;
                        }
                        i++;
                    }
                    timers.remove(id);
                    PId.remove(id);
                    int currIndex = 0;
                    while(currIndex < data.size()){
                        String currArrayNodeCsID = data.get(currIndex).get(0).get("CS_ID").asText();
                        if (currArrayNodeCsID.equals(id)){
                            data.remove(currIndex);
                            Utils.updateAggregationServerBackUpFile(data, lampClock.getCurrTime(), CSId, PId, PIdCounter);
                            continue;
                        }
                        currIndex += 1;
                    }
                }
            }
        }, 30000);
    }

    public static void killTimer(String id) {
        if (timers.containsKey(id)) {
            timers.get(id).cancel();
            timers.remove(id);
        }
    }
    public static void main(String[] args) {
        try {
            int portNumber = 4567;
            if (args.length > 0) {
                portNumber =Integer.parseInt(args[0]);
            }
            System.out.println("Aggregation Server has started...");
            ServerSocket serSocket = new ServerSocket(portNumber);
            File JSONFile = new File("backup_data/backup.json");
            if (JSONFile.exists()) {
                ObjectMapper objectMapper = new ObjectMapper();
                data = (ArrayNode) objectMapper.readTree(new File("backup_data/backup.json")).get("data");
                CSId = (ArrayNode) objectMapper.readTree(new File("backup_data/backup.json")).get("All_CS_ID");
                PId = (ObjectNode) objectMapper.readTree(new File("backup_data/backup.json")).get("All_Process_ID");
                PIdCounter = (ArrayNode) objectMapper.readTree(new File("backup_data/backup.json")).get("Process_ID");
                for (int i = 0; i < CSId.size(); i++) {
                    JsonNode node = CSId.get(i);
                    activateTimer(node.asText());
                }
                lampClock.updateTime(objectMapper.readTree(new File("backup_data/backup.json")).get("Lamport").asInt());
            } else {
                data = new ObjectMapper().createArrayNode();
            }
            while (true) {
                Socket socket = serSocket.accept();
                new Thread(new clientReqHandler(lampClock, data, timers, socket, CSId, PId, PIdCounter)).start();
            }
        } catch (Exception e) {
            System.err.println("Please try again!\nSomething went wrong while starting Aggregation Server Backup.");
            System.exit(1);
        }
    }
}

class clientReqHandler implements Runnable {
    private final LamportClock clock;
    private final ArrayNode allData;
    private final Map<String, Timer> timers;
    private Socket clientSocket;
    private final ArrayNode IDS;
    private final ObjectNode allProcessId;
    private final ArrayNode processIdCounter;
    public clientReqHandler(LamportClock clock, ArrayNode allData, Map<String, Timer> timers, Socket clientSocket, ArrayNode IDS, ObjectNode allProcessId, ArrayNode processIdCounter) {
        this.clock = clock;
        this.allData = allData;
        this.timers = timers;
        this.clientSocket = clientSocket;
        this.IDS = IDS;
        this.allProcessId = allProcessId;
        this.processIdCounter = processIdCounter;
    }

    @Override
    public void run() {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String res;
            Object[] headAndBody = Utils.handleHeaderAndBody(rd);
            OutputStream opStream = clientSocket.getOutputStream();
            Map<String, String> headersMap = (Map<String, String>) headAndBody[0];
            ArrayNode body = (ArrayNode) headAndBody[1];
            if (headersMap.get("method").contains("GET")){

                if (headersMap.containsKey("Lamport-Clock")){
                    clock.updateTime(Integer.parseInt(headersMap.get("Lamport-Clock")));
                }else{
                    clock.updateTime(0);
                }
                res = get(headersMap, allData, clock);
            }else if(headersMap.get("method").contains("PUT")){
                ArrayNode allOldCsId = IDS.deepCopy();
                String CS_ID = headersMap.get("CS-ID");
                synchronized (processIdCounter) {
                    if (!allProcessId.has(CS_ID)) {
                        int currentProcessID = processIdCounter.get(0).asInt() + 1;
                        processIdCounter.set(0, currentProcessID);
                        allProcessId.put(CS_ID, currentProcessID);
                    }
                }
                ArrayNode receivedData = Utils.compareDuplicatesIfAny(body, CS_ID, clock.getCurrTime(), allProcessId);
                synchronized (allData) {
                    allData.add(receivedData);
                    if (allData.size() > 20) {
                        allData.remove(0);
                    }
                }
                if (timers.containsKey(CS_ID)) {
                    AggregationServer.activateTimer(CS_ID);
                    AggregationServer.killTimer(CS_ID);
                } else {
                    IDS.add(CS_ID);
                    AggregationServer.activateTimer(CS_ID);
                }
                clock.updateTime(Integer.parseInt(headersMap.get("Lamport-Clock")));
                res = put(CS_ID, allData, clock, allOldCsId);
                Utils.updateAggregationServerBackUpFile(allData, clock.getCurrTime(), IDS, allProcessId, processIdCounter);
            }else{
                res = "HTTP/1.1 400 Bad Request\r\n";
            }
            opStream.write(res.getBytes());
            opStream.flush();
        } catch (IOException e) {
            System.err.println("Please Try Again!.\nSomething went wrong while accepting the connection from other sockets in the Aggregation Server.");
            System.exit(1);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Handle GET from the Client
    public static String get(Map<String, String> header, ArrayNode data, LamportClock clock) throws JsonProcessingException {
        String res = "HTTP/1.1 400 Bad Request\r\n" + "Lamport-Clock: " + clock.getCurrTime() + "\r\n\r\n";
        if (header.containsKey("CS-ID")){
            res = "HTTP/1.1 200 OK\r\n" + "Lamport-Clock: " + clock.getCurrTime() + "\r\n\r\n";
        }else if(header.get("method").contains("stationId=")){
            Pattern pattern = Pattern.compile("stationId=([^\\s]+)");
            Matcher matcher = pattern.matcher(header.get("method"));
            if (matcher.find()) {
                String stationID = matcher.group(1);
                ArrayNode mainArr = new ObjectMapper().createArrayNode();
                mainArr.add(lamportClockTimeBreakingMechanism(data, stationID));
                if (!mainArr.get(0).isEmpty()) {
                    String LamportClockHeader = "Lamport-Clock: " + clock.getCurrTime() + "\r\n";
                    String mainArrString = new ObjectMapper().writeValueAsString(mainArr);
                    String ContentText = "Content-Type: application/json\r\n" + "Content-Length: " + mainArrString.length() + "\r\n\r\n";
                    res = "HTTP/1.1 200 OK\r\n" + LamportClockHeader + ContentText + mainArrString;
                }
            } else {
                String LamportClockHeader = "Lamport-Clock: " + clock.getCurrTime() + "\r\n";
                String mainArrString = new ObjectMapper().writeValueAsString(data);
                String ContentText = "Content-Type: application/json\r\n"+ "Content-Length: " + mainArrString.length() + "\r\n\r\n";
                res = "HTTP/1.1 200 OK\r\n" + LamportClockHeader + ContentText + mainArrString;
            }
        }
        return res;
    }

    private static ArrayNode lamportClockTimeBreakingMechanism(JsonNode node, String targetId) {
        ArrayNode resArr = new ObjectMapper().createArrayNode();
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrNode = node.get(i);
                if (arrNode.isArray()) {
                    int j=0;
                    while(j < arrNode.size()){
                        JsonNode objNode = arrNode.get(j);
                        if (objNode.has("id") && objNode.isObject() && objNode.get("id").asText().equals(targetId)) {
                            int currentPID = objNode.get("processId").asInt();
                            int currLamport = objNode.get("Aggregation_current_lamport").asInt();
                            if (resArr.isEmpty()) {
                                resArr.add(objNode);
                            } else if (currLamport == resArr.get(0).get("Aggregation_current_lamport").asInt()) {
                                if (currentPID > resArr.get(0).get("processId").asInt()) {
                                    resArr.add(objNode);
                                }
                            } else if (currLamport > resArr.get(0).get("Aggregation_current_lamport").asInt()) {
                                resArr.removeAll();
                                resArr.add(objNode);
                            }
                        }
                    }
                }
            }
        }        
        return resArr;
    }

    // Handle PUT from the Content Server
    public static String put(String CS_ID, ArrayNode data, LamportClock clock, ArrayNode allOldCsId) throws IOException {
        File JSONFile = new File("backup_data/backup.json");
        int currentTime = clock.getCurrTime();
        String lamportHeader = "Lamport-Clock: " + currentTime + "\r\n\r\n";
        String res = "HTTP/1.1 400 Bad Request\r\n" + lamportHeader;
        
        if (data.get(0).size() == 0){
            res = "HTTP/1.1 204 No Content\r\n" + lamportHeader;
        }else if(!JSONFile.exists()) {
            if (JSONFile.createNewFile()) {
                RandomAccessFile raf = new RandomAccessFile(JSONFile, "rw");
                raf.setLength(1024*1024);
                res = "HTTP/1.1 201 - HTTP_CREATED\r\n" + lamportHeader;
                raf.close();
            }
        } else {
            if (containVal(allOldCsId, CS_ID)) {
                res = "HTTP/1.1 200 OK\r\n" + lamportHeader;            
            } 
            else {
                res = "HTTP/1.1 201 - HTTP_CREATED\r\n" + lamportHeader;
            }
        }

        return res;
    }

    public static boolean containVal(ArrayNode arrayNode, String value) {
        int i=0;
        while(i<arrayNode.size()){
            if (arrayNode.get(i).asText().equals(value)) {
                return true;
            }
            i++;
        }
        return false;
    }
}